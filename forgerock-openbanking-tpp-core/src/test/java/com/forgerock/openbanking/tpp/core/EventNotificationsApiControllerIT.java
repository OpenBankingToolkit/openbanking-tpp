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
package com.forgerock.openbanking.tpp.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.oidc.services.UserInfoService;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.model.PaymentEvent;
import com.forgerock.openbanking.tpp.core.model.PaymentStatusRequest;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.repository.PaymentEventsRepository;
import com.forgerock.openbanking.tpp.core.repository.PaymentStatusRequestRepository;
import com.forgerock.openbanking.tpp.core.scheduler.PaymentsStatusService;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.AspspAsService;
import com.forgerock.openbanking.tpp.core.services.aspsp.rs.RSPaymentAPIService;
import kong.unirest.HttpResponse;
import kong.unirest.JacksonObjectMapper;
import kong.unirest.Unirest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import uk.org.openbanking.OBHeaders;
import uk.org.openbanking.datamodel.payment.OBPaymentDataSubmissionResponse1;
import uk.org.openbanking.datamodel.payment.OBTransactionIndividualStatus1Code;
import uk.org.openbanking.datamodel.payment.paymentsubmission.OBPaymentSubmissionResponse1;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventNotificationsApiControllerIT {
    private static final Logger log = LoggerFactory.getLogger(EventNotificationsApiControllerIT.class);

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserInfoService userInfoService;


    @Autowired
    private PaymentsStatusService paymentsStatusService;

    @MockBean
    private AspspAsService aspspAsService;

    @MockBean
    private AspspConfigurationMongoRepository aspspConfigurationRepository;

    @MockBean
    private RSPaymentAPIService rsPaymentAPIService;

    @Autowired
    private PaymentStatusRequestRepository paymentStatusRequestRepository;

    @Autowired
    private PaymentEventsRepository paymentEventsRepository;

    @Before
    public void setUp() throws Exception {
        Unirest.config().setObjectMapper(new JacksonObjectMapper(objectMapper)).verifySsl(false);

        // Mock out the ASP credential services for this test as we use mock RS-Store
        given(aspspConfigurationRepository.findById(any())).willReturn(Optional.of(new AspspConfiguration()));
        given(aspspAsService.clientCredential(any(), any())).willReturn(new AccessTokenResponse());
    }

    @Test
    public void notifyTppOfCompletedPayment_findMatchingPaymentRequest_deletePaymentRequest_createPaymentEvent() throws Exception {
        // Given ('completed' payment in RS Store, 'In progress' payment request in TPP shop and Notification JWT for completed payment)
        final String resourceId = "PDC_6649e82c-a31c-4876-8018-ca4738d74cb9";
        final String resourceUrl = "https://rs-store:8086/v3.0/domestic-payments/"+resourceId;
        final String serialisedJwtNotification = "eyJraWQiOiIwZjYyMzFkMzA1NzU4MjI0MDczMTJiZDJjOWVlMzJiZmE4ZTc5ZDBlIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJodHRwczpcL1wvcnMtc3RvcmU6ODA4NlwvdjMuMFwvZG9tZXN0aWMtcGF5bWVudHNcL1BEQ182NjQ5ZTgyYy1hMzFjLTQ4NzYtODAxOC1jYTQ3MzhkNzRjYjkiLCJhdWQiOiJmb3JnZXJvY2stcmNzIiwiaXNzIjoiZm9yZ2Vyb2NrLXJjcyIsInR4biI6ImUwMTllNzMzLTE4ODktNDkzYS1hN2QzLTE0MTk3ZjczODJkZiIsInRvZSI6MTU0NDQ1MzU5MSwiaWF0IjoxNTQ0NDUzNTkxLCJqdGkiOiI2NjgwMGJlMS0zYTg3LTQxMGItODRjNS1kZTY1YTE4YmM0N2EiLCJldmVudHMiOnsidXJuOnVrOm9yZzpvcGVuYmFua2luZzpldmVudHM6cmVzb3VyY2UtdXBkYXRlIjp7InN1YmplY3QiOnsiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9yaWQiOiJQRENfNjY0OWU4MmMtYTMxYy00ODc2LTgwMTgtY2E0NzM4ZDc0Y2I5Iiwic3ViamVjdF90eXBlIjoiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9yaWRfaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9ydHkiLCJodHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3JsayI6W3sibGluayI6Imh0dHBzOlwvXC9ycy1zdG9yZTo4MDg2XC92My4wXC9kb21lc3RpYy1wYXltZW50c1wvUERDXzY2NDllODJjLWEzMWMtNDg3Ni04MDE4LWNhNDczOGQ3NGNiOSIsInZlcnNpb24iOiJ2My4wIn1dLCJodHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3J0eSI6ImRvbWVzdGljLXBheW1lbnQifX19fQ.LfvlQhUIx7QnKVMvKSubJFaHUCH1HIZVR8Id9RG5nNO_2tkM6uWoy87eXnKWVyFJX9-Tw6j1ageaO9NBDjQzBPJMkBzRQnLU0aGOvDds20tOh4YaDEj_XdUP44ovZysqsoW51frThudIsgRJAeCytngADpDrzT48ba9gBVRkZEMP7FRVMVnenfV-k-lLhBIlyDI3QkAWHdDWJIzIiEROR0GMFdW3t40ZI6UeeJp1MfE4MHthakkDGMcWdWUBuy_xEKvcoEmPFvh4ue6iVlujguHENZSDWrPv7feW_2qAopdHlpnjci4HLiAvyENZTSNeFiNoAnRVaeuhoGaoyl3hJA";
        paymentStatusRequestRepository.save(
                new PaymentStatusRequest()
                        .paymentSubmissionId(resourceId)
                        .aspspId("FR")
                        .status(OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTINPROCESS)
        );
        given(rsPaymentAPIService.getPaymentSubmission(eq(resourceUrl), any(AccessTokenResponse.class)))
            .willReturn(new OBPaymentSubmissionResponse1()
                .data(new OBPaymentDataSubmissionResponse1()
                        .paymentId(resourceId)
                        .status(OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTCOMPLETED)
                )
            );

        // When (notify TPP about completed payment)
        HttpResponse<String> response = Unirest.post("https://tpp-core:" + port + "/open-banking/v3.0/event-notifications")
                .header(OBHeaders.X_FAPI_FINANCIAL_ID, "54321")
                .header(OBHeaders.CONTENT_TYPE, "application/jwt; charset=utf-8")
                .header(OBHeaders.X_FAPI_INTERACTION_ID, "12345")
                .body(serialisedJwtNotification)
                .asObject(String.class);

        // Then ('in progress' payment request is deleted and completed payment event created)
        assertThat(response.getStatus()).isEqualTo(202);

        final List<PaymentEvent> allEvents = paymentEventsRepository.findAll();
        assertThat(allEvents.size()).isEqualTo(1);
        PaymentEvent newPaymentEvent = allEvents.get(0);
        assertThat(newPaymentEvent.getPaymentId()).isEqualTo(resourceId);
        assertThat(newPaymentEvent.getStatus()).isEqualTo(OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTCOMPLETED);
        assertThat(paymentStatusRequestRepository.findAll().isEmpty()).isTrue();

        // Clean-up
        paymentEventsRepository.deleteAll();
    }

    @Test
    public void notifyTppOfCompletedPayment_noMatchingPaymentRequest_takeNoAction() throws Exception {
        // Given
        final String resourceId = "PDC_6649e82c-a31c-4876-8018-ca4738d74cb9";
        final String resourceUrl = "https://rs-store:8086/v3.0/domestic-payments/"+resourceId;
        final String serialisedJwtNotification = "eyJraWQiOiIwZjYyMzFkMzA1NzU4MjI0MDczMTJiZDJjOWVlMzJiZmE4ZTc5ZDBlIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJodHRwczpcL1wvcnMtc3RvcmU6ODA4NlwvdjMuMFwvZG9tZXN0aWMtcGF5bWVudHNcL1BEQ182NjQ5ZTgyYy1hMzFjLTQ4NzYtODAxOC1jYTQ3MzhkNzRjYjkiLCJhdWQiOiJmb3JnZXJvY2stcmNzIiwiaXNzIjoiZm9yZ2Vyb2NrLXJjcyIsInR4biI6ImUwMTllNzMzLTE4ODktNDkzYS1hN2QzLTE0MTk3ZjczODJkZiIsInRvZSI6MTU0NDQ1MzU5MSwiaWF0IjoxNTQ0NDUzNTkxLCJqdGkiOiI2NjgwMGJlMS0zYTg3LTQxMGItODRjNS1kZTY1YTE4YmM0N2EiLCJldmVudHMiOnsidXJuOnVrOm9yZzpvcGVuYmFua2luZzpldmVudHM6cmVzb3VyY2UtdXBkYXRlIjp7InN1YmplY3QiOnsiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9yaWQiOiJQRENfNjY0OWU4MmMtYTMxYy00ODc2LTgwMTgtY2E0NzM4ZDc0Y2I5Iiwic3ViamVjdF90eXBlIjoiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9yaWRfaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9ydHkiLCJodHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3JsayI6W3sibGluayI6Imh0dHBzOlwvXC9ycy1zdG9yZTo4MDg2XC92My4wXC9kb21lc3RpYy1wYXltZW50c1wvUERDXzY2NDllODJjLWEzMWMtNDg3Ni04MDE4LWNhNDczOGQ3NGNiOSIsInZlcnNpb24iOiJ2My4wIn1dLCJodHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3J0eSI6ImRvbWVzdGljLXBheW1lbnQifX19fQ.LfvlQhUIx7QnKVMvKSubJFaHUCH1HIZVR8Id9RG5nNO_2tkM6uWoy87eXnKWVyFJX9-Tw6j1ageaO9NBDjQzBPJMkBzRQnLU0aGOvDds20tOh4YaDEj_XdUP44ovZysqsoW51frThudIsgRJAeCytngADpDrzT48ba9gBVRkZEMP7FRVMVnenfV-k-lLhBIlyDI3QkAWHdDWJIzIiEROR0GMFdW3t40ZI6UeeJp1MfE4MHthakkDGMcWdWUBuy_xEKvcoEmPFvh4ue6iVlujguHENZSDWrPv7feW_2qAopdHlpnjci4HLiAvyENZTSNeFiNoAnRVaeuhoGaoyl3hJA";
        paymentStatusRequestRepository.save(
                new PaymentStatusRequest()
                        .paymentSubmissionId("SomeOtherPayment")
                        .aspspId("FR")
                        .status(OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTINPROCESS)
        );
        given(rsPaymentAPIService.getPaymentSubmission(eq(resourceUrl), any(AccessTokenResponse.class)))
                .willReturn(new OBPaymentSubmissionResponse1()
                        .data(new OBPaymentDataSubmissionResponse1()
                                .paymentId(resourceId)
                                .status(OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTCOMPLETED)
                        )
                );


        // When (notify TPP about completed payment)
        HttpResponse<String> response = Unirest.post("https://tpp-core:" + port + "/open-banking/v3.0/event-notifications")
                .header(OBHeaders.X_FAPI_FINANCIAL_ID, "54321")
                .header(OBHeaders.CONTENT_TYPE, "application/jwt; charset=utf-8")
                .header(OBHeaders.X_FAPI_INTERACTION_ID, "12345")
                .body(serialisedJwtNotification)
                .asObject(String.class);

        // Then (No action but return 202)
        assertThat(response.getStatus()).isEqualTo(202);
        assertThat(paymentEventsRepository.findAll().size()).isEqualTo(0);
        assertThat(paymentStatusRequestRepository.findAll().size()).isEqualTo(1);

        // Clean-up
        paymentStatusRequestRepository.deleteAll();
    }
}