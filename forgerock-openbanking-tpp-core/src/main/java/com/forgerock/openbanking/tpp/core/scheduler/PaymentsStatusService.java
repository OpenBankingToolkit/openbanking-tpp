/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.scheduler;

import com.forgerock.openbanking.jwt.exceptions.InvalidTokenException;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.tpp.core.model.PaymentEvent;
import com.forgerock.openbanking.tpp.core.model.PaymentStatusRequest;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.repository.PaymentEventsRepository;
import com.forgerock.openbanking.tpp.core.repository.PaymentStatusRequestRepository;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.AspspAsService;
import com.forgerock.openbanking.tpp.core.services.aspsp.rs.RSPaymentAPIService;
import com.forgerock.openbanking.tpp.exceptions.InvalidAuthorizationCodeException;

import com.forgerock.openbanking.tpp.model.notification.ResourceUpdateEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.paymentsubmission.OBPaymentSubmissionResponse1;

import java.util.Optional;

import static com.forgerock.openbanking.tpp.core.api.payment.PaymentRequestsController.PISP_SCOPES;


@Service
@Slf4j
public class PaymentsStatusService {

    private AspspAsService aspspAsService;
    private AspspConfigurationMongoRepository aspspConfigurationRepository;
    private RSPaymentAPIService rsPaymentAPIService;
    private PaymentStatusRequestRepository paymentStatusRequestRepository;
    private PaymentEventsRepository paymentEventsRepository;

    @Autowired
    public PaymentsStatusService(AspspAsService aspspAsService, AspspConfigurationMongoRepository aspspConfigurationRepository, RSPaymentAPIService rsPaymentAPIService, PaymentStatusRequestRepository paymentStatusRequestRepository, PaymentEventsRepository paymentEventsRepository) {
        this.aspspAsService = aspspAsService;
        this.aspspConfigurationRepository = aspspConfigurationRepository;
        this.rsPaymentAPIService = rsPaymentAPIService;
        this.paymentStatusRequestRepository = paymentStatusRequestRepository;
        this.paymentEventsRepository = paymentEventsRepository;
    }

    public void updatePayment(final ResourceUpdateEvent resourceUpdateEvent) {
        if (!resourceUpdateEvent.getFirstResourceLink().isPresent()) {
            log.warn("No resource links provided with resource update event ({}). Unable to update payment.", resourceUpdateEvent);
            return;
        }

        // Is there a matching payment status request in TPP to changed resource on ASPSP?
        final Optional<PaymentStatusRequest> ifPaymentStatusRequest = paymentStatusRequestRepository.findAll()
                .stream()
                .filter(paymentRequest -> paymentRequest.getPaymentSubmissionId().equals(resourceUpdateEvent.getSubject().getResourceId()))
                .findFirst();

        if (ifPaymentStatusRequest.isPresent()) {
            PaymentStatusRequest paymentStatusRequest = ifPaymentStatusRequest.get();
            Optional<AccessTokenResponse> accessTokenResponse = getAccessTokenForAspsp(paymentStatusRequest);
            if (!accessTokenResponse.isPresent()) {
                log.warn("No Access Token available for ASPSP: {}. Unable to update payment.", paymentStatusRequest.getAspspId());
                return;
            }

            try {
                OBPaymentSubmissionResponse1 payment = rsPaymentAPIService.getPaymentSubmission(resourceUpdateEvent.getFirstResourceLink().get(), accessTokenResponse.get());
                updateSavedPayment(payment, paymentStatusRequest);
                updateSavedStatus(paymentStatusRequest);
                log.info("Payment '{}' for ASPSP '{}' is now updated from notification event.", resourceUpdateEvent.getSubject().getResourceId(), paymentStatusRequest.getAspspId());
            } catch (InvalidAuthorizationCodeException e) {
                log.warn("Couldn't get the new status for payment submission id {}",
                        paymentStatusRequest.getPaymentSubmissionId(), e);
            }

        } else {
            log.info("No payment requests to update for resource: {}", resourceUpdateEvent.getSubject().getResourceId());
        }

    }


    private void updateSavedPayment(OBPaymentSubmissionResponse1 payment, PaymentStatusRequest paymentStatusRequest) {
        if (!paymentStatusRequest.getStatus().equals(payment.getData().getStatus())) {
            paymentStatusRequest.status(payment.getData().getStatus());
            PaymentEvent paymentEvent = new PaymentEvent()
                    .paymentSubmissionId(payment.getData().getPaymentSubmissionId())
                    .paymentId(payment.getData().getPaymentId())
                    .paymentRequest(paymentStatusRequest.getPaymentRequest())
                    .status(payment.getData().getStatus()
                    );
            log.debug("Save new payment event: {}", paymentEvent);
            paymentEventsRepository.save(paymentEvent);
        }
    }

    private void updateSavedStatus(PaymentStatusRequest paymentStatusRequest) {
        switch (paymentStatusRequest.getStatus()) {
            case ACCEPTEDCUSTOMERPROFILE:
            case ACCEPTEDSETTLEMENTINPROCESS:
            case ACCEPTEDTECHNICALVALIDATION:
            case PENDING:
                paymentStatusRequestRepository.save(paymentStatusRequest);
                break;
            case ACCEPTEDSETTLEMENTCOMPLETED:
            case REJECTED:
                paymentStatusRequestRepository.delete(paymentStatusRequest);
                break;
        }

    }

    private Optional<AccessTokenResponse> getAccessTokenForAspsp(PaymentStatusRequest paymentStatusRequest) {
        log.debug("Get ASPSP configuration for {} and get an access token", paymentStatusRequest.getAspspId());
        final String aspspId = paymentStatusRequest.getAspspId();

        return aspspConfigurationRepository.findById(aspspId)
                .map(cfg -> {
                    try {
                        return aspspAsService.clientCredential(cfg, PISP_SCOPES);
                    } catch (InvalidTokenException e) {
                        log.warn("Couldn't get access token for ASPSP ID {}", aspspId, e);
                        return null;
                    }
                });
    }
}
