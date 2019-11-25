/**
 * Copyright 2019 ForgeRock AS.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.forgerock.openbanking.tpp.core.scheduler;

import com.forgerock.openbanking.jwt.exceptions.InvalidTokenException;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.model.PaymentEvent;
import com.forgerock.openbanking.tpp.core.model.PaymentStatusRequest;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.repository.PaymentEventsRepository;
import com.forgerock.openbanking.tpp.core.repository.PaymentStatusRequestRepository;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.AspspAsService;
import com.forgerock.openbanking.tpp.core.services.aspsp.rs.RSPaymentAPIService;
import com.forgerock.openbanking.tpp.exceptions.InvalidAuthorizationCodeException;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.payment.paymentsubmission.OBPaymentSubmissionResponse1;

import java.util.*;
import java.util.stream.Collectors;

import static com.forgerock.openbanking.constants.OpenBankingConstants.BOOKED_TIME_DATE_FORMAT;
import static com.forgerock.openbanking.tpp.core.api.payment.PaymentRequestsController.PISP_SCOPES;

@Component
public class PullingPaymentsStatusTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(PullingPaymentsStatusTask.class);
    private final static DateTimeFormatter format = DateTimeFormat.forPattern(BOOKED_TIME_DATE_FORMAT);

    @Autowired
    private AspspAsService aspspAsService;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;
    @Autowired
    private RSPaymentAPIService rsPaymentAPIService;
    @Autowired
    private PaymentStatusRequestRepository paymentStatusRequestRepository;
    @Autowired
    private PaymentEventsRepository paymentEventsRepository;

    @Scheduled(fixedRate = 60 * 1000)
    @SchedulerLock(name = "pullingASPSP")
    public void pullingASPSP() {
        LOGGER.info("Pulling payments status task waking up. The time is now {}.", format.print(DateTime.now()));

        List<PaymentStatusRequest> paymentStatusRequests = paymentStatusRequestRepository.findAll();
        if (paymentStatusRequests.isEmpty()) {
            LOGGER.info("Nothing to pull from ASPSPs. See you in 5 minutes! The time is now {}.", format.print(DateTime.now()));
            return;
        }

        Set<String> aspspIds = paymentStatusRequests.stream().map(p -> p.getAspspId()).distinct().collect(Collectors.toSet());
        LOGGER.info("There is '{}' payments to compute for {} ASPSPs", paymentStatusRequests.size(), aspspIds);

        Map<String, AccessTokenResponse> accessTokenForEachASPSP = new HashMap<>();
        Map<String, AspspConfiguration> aspspConfigurationByASPSP = new HashMap<>();

        LOGGER.info("Get ASPSP configuration and get an access token for each of them");
        for (String aspspId: aspspIds) {
            Optional<AspspConfiguration> aspspConfigurationOptional = aspspConfigurationRepository.findById(aspspId);
            if (!aspspConfigurationOptional.isPresent()) {
                LOGGER.info("ASPSP ID {} doesn't exist.", aspspId);
                continue;
            }
            AspspConfiguration aspspConfiguration = aspspConfigurationOptional.get();
            aspspConfigurationByASPSP.put(aspspId, aspspConfiguration);
            try {
                AccessTokenResponse accessTokenResponse = aspspAsService.clientCredential(aspspConfiguration, PISP_SCOPES);
                accessTokenForEachASPSP.put(aspspId, accessTokenResponse);
            } catch (InvalidTokenException e) {
                LOGGER.warn("Couldn't get access token for ASPSP ID {}", aspspId, e);
                continue;
            }
        }

        LOGGER.info("Get payment status for each payments.");
        List<PaymentStatusRequest> paymentStatusRequestsThatSucceed = new ArrayList<>();
        List<PaymentEvent> events = new ArrayList<>();
        for (PaymentStatusRequest paymentStatusRequest: paymentStatusRequests) {
            AspspConfiguration aspspConfiguration = aspspConfigurationByASPSP.get(paymentStatusRequest.getAspspId());
            if (!accessTokenForEachASPSP.containsKey(paymentStatusRequest.getAspspId())) {
                LOGGER.warn("Skipping payment submission ID {} as couldn't get earlier an access token for ASPSP ID {}",
                        paymentStatusRequest.getPaymentSubmissionId(), paymentStatusRequest.getAspspId());
                continue;
            }
            AccessTokenResponse accessTokenResponse = accessTokenForEachASPSP.get(paymentStatusRequest.getAspspId());
            try {
                OBPaymentSubmissionResponse1 status = rsPaymentAPIService.getPaymentSubmission(aspspConfiguration,
                        paymentStatusRequest.getPaymentSubmissionId(), accessTokenResponse);
                if (!paymentStatusRequest.getStatus().equals(status.getData().getStatus())) {
                    paymentStatusRequest.status(status.getData().getStatus());
                    events.add(new PaymentEvent()
                            .paymentSubmissionId(status.getData().getPaymentSubmissionId())
                            .paymentId(status.getData().getPaymentId())
                            .paymentRequest(paymentStatusRequest.getPaymentRequest())
                            .status(status.getData().getStatus())
                    );
                }
                paymentStatusRequestsThatSucceed.add(paymentStatusRequest);
            } catch (InvalidAuthorizationCodeException e) {
                LOGGER.warn("Couldn't get the new status for payment submission id {}",
                        paymentStatusRequest.getPaymentSubmissionId(), e);
                continue;
            }
        }

        //We remove the payment that are now completed and won't potentially change status.
        List<PaymentStatusRequest> toSave = new ArrayList<>();
        List<PaymentStatusRequest> toDelete = new ArrayList<>();
        for (PaymentStatusRequest paymentStatusRequest: paymentStatusRequestsThatSucceed) {
            switch (paymentStatusRequest.getStatus()) {

                case ACCEPTEDCUSTOMERPROFILE:
                case ACCEPTEDSETTLEMENTINPROCESS:
                case ACCEPTEDTECHNICALVALIDATION:
                case PENDING:
                    toSave.add(paymentStatusRequest);
                    break;
                case ACCEPTEDSETTLEMENTCOMPLETED:
                case REJECTED:
                    toDelete.add(paymentStatusRequest);
                    break;
            }
        }
        LOGGER.info("Save new payment events ");
        paymentEventsRepository.saveAll(events);
        LOGGER.info("'{}' payments status still need to be refreshed and {} are completed", toSave.size(), toDelete.size());
        paymentStatusRequestRepository.saveAll(toSave);
        paymentStatusRequestRepository.deleteAll(toDelete);

        LOGGER.info("All payments are now processed. See you in 5 minutes! The time is now {}.",
                format.print(DateTime.now()));
    }
}
