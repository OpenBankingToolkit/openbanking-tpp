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
package com.forgerock.openbanking.tpp.core.services.aspsp.rs;

import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.configuration.RSConfiguration;
import com.forgerock.openbanking.tpp.core.model.FRPaymentSetup;
import com.forgerock.openbanking.tpp.exceptions.InvalidAuthorizationCodeException;
import com.forgerock.openbanking.tpp.exceptions.RegistrationFailure;
import com.forgerock.openbanking.tpp.model.openbanking.ConsentStatusCode;
import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.OBHeaders;
import uk.org.openbanking.datamodel.payment.OBPaymentDataSubmission1;
import uk.org.openbanking.datamodel.payment.paymentsetup.OBPaymentSetup1;
import uk.org.openbanking.datamodel.payment.paymentsetup.OBPaymentSetupResponse1;
import uk.org.openbanking.datamodel.payment.paymentsubmission.OBPaymentSubmission1;
import uk.org.openbanking.datamodel.payment.paymentsubmission.OBPaymentSubmissionResponse1;

import java.util.Collections;
import java.util.UUID;

/**
 * OIDC flows contains the functions needed by an OpenID client.
 */
@Service
public class RSPaymentAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSPaymentAPIService.class);

    @Autowired
    private RSConfiguration rsConfiguration;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Register a new payment
     *
     * @param paymentRequest       the order associated with the payment
     * @param accessTokenResponse the access token for registering the payment
     * @return the payment response from the RS
     * @throws RegistrationFailure
     */
    public FRPaymentSetup registerPayment(AspspConfiguration aspspConfiguration, FRPaymentRequest1 paymentRequest, AccessTokenResponse accessTokenResponse)
            throws RegistrationFailure {
        LOGGER.debug("Register a new payment to the RS, for the payment request {}", paymentRequest);

        String uid = UUID.randomUUID().toString();

        //Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessTokenResponse.access_token);
        headers.add(OBHeaders.X_IDEMPOTENCY_KEY, paymentRequest.getOrderId());
        //It's optional and can probably be replaced by a JWS content instead.
        //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
        headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, aspspConfiguration.getFinancialId());
        //We don't have the user last logged time
        //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
        headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
        headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
        headers.add(OBHeaders.ACCEPT, "application/json");

        //Send request
        HttpEntity<OBPaymentSetup1> request = new HttpEntity<>(paymentRequest.getPaymentSetup(), headers);

        try {
            OBPaymentSetupResponse1 obPaymentSetupResponse1 = restTemplate.postForObject(
                    aspspConfiguration.getDiscoveryAPILinksPayment().getCreateSingleImmediatePayment(), request, OBPaymentSetupResponse1.class);
            FRPaymentSetup paymentSetup = new FRPaymentSetup();
            paymentSetup.setPaymentSetupRequest(paymentRequest.getPaymentSetup());
            paymentSetup.setId(obPaymentSetupResponse1.getData().getPaymentId());
            paymentSetup.setStatus(ConsentStatusCode.fromValue(obPaymentSetupResponse1.getData().getStatus().toString()));
            return paymentSetup;
        } catch (HttpClientErrorException e) {
            LOGGER.error("Could not register payment to RS", e);
            throw new RegistrationFailure(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * Send a payment submission
     *
     * @param paymentID           the payment ID we want to submit
     * @param accessTokenResponse the access token to have the authorization of making a payment submission to the RS
     * @return the payment response
     * @throws InvalidAuthorizationCodeException
     */
    public OBPaymentSubmissionResponse1 paymentSubmission(AspspConfiguration aspspConfiguration,
                                                          FRPaymentSetup paymentSetup, String paymentID,
                                                          AccessTokenResponse accessTokenResponse)
            throws InvalidAuthorizationCodeException {
        LOGGER.debug("We send a payment submission for the payment ID '{}'", paymentID);

        //Request body
        OBPaymentDataSubmission1 data = new OBPaymentDataSubmission1()
                .paymentId(paymentID)
                .initiation(paymentSetup.getInitiation());

        OBPaymentSubmission1 paymentSubmission = new OBPaymentSubmission1()
                .data(data)
                .risk(paymentSetup.getPaymentSetupRequest().getRisk());

        String uid = UUID.randomUUID().toString();
        //Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessTokenResponse.access_token);
        headers.add(OBHeaders.X_IDEMPOTENCY_KEY, uid);
        //It's optional and can probably be replaced by a JWS content instead.
        //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
        headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
        //We don't have the user last logged time
        //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
        headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
        headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
        headers.add(OBHeaders.CONTENT_TYPE, "application/json");
        headers.add(OBHeaders.ACCEPT, "application/json");

        //Send request
        HttpEntity<OBPaymentSubmission1> request = new HttpEntity<>(paymentSubmission, headers);
        try {
            return restTemplate.postForObject(aspspConfiguration.getDiscoveryAPILinksPayment().getCreatePaymentSubmission(),
                    request, OBPaymentSubmissionResponse1.class);
        } catch (HttpClientErrorException e) {
            throw new InvalidAuthorizationCodeException(e.getResponseBodyAsString(), e);
        }
    }


    /**
     * Get payment submission status
     *
     * @param paymentSubmissionId           the payment submission ID
     * @param accessTokenResponse the access token to have the authorization of making a payment submission to the RS
     * @return the payment submission response
     * @throws InvalidAuthorizationCodeException
     */
    public OBPaymentSubmissionResponse1 getPaymentSubmission(AspspConfiguration aspspConfiguration,
                                                             String paymentSubmissionId,
                                                             AccessTokenResponse accessTokenResponse)
            throws InvalidAuthorizationCodeException {
        LOGGER.debug("We send a payment submission for the payment ID '{}'", paymentSubmissionId);


        String uid = UUID.randomUUID().toString();
        //Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessTokenResponse.access_token);
        headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
        //We don't have the user last logged time
        headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
        headers.add(OBHeaders.ACCEPT, "application/json");

        //Send request
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                aspspConfiguration.getDiscoveryAPILinksPayment().getGetPaymentSubmission());
        try {
            ParameterizedTypeReference<OBPaymentSubmissionResponse1> ptr = new ParameterizedTypeReference<OBPaymentSubmissionResponse1>() {};

            return restTemplate.exchange(builder.build(Collections.singletonMap("PaymentSubmissionId", paymentSubmissionId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr).getBody();
        } catch (HttpClientErrorException e) {
            throw new InvalidAuthorizationCodeException(e.getResponseBodyAsString(), e);
        }
    }

    public OBPaymentSubmissionResponse1 getPaymentSubmission(String resourceLink, AccessTokenResponse accessTokenResponse)
            throws InvalidAuthorizationCodeException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessTokenResponse.access_token);
        headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
        //We don't have the user last logged time
        headers.add(OBHeaders.X_FAPI_INTERACTION_ID,  UUID.randomUUID().toString());
        headers.add(OBHeaders.ACCEPT, "application/json");

        //Send request
        try {
            ParameterizedTypeReference<OBPaymentSubmissionResponse1> ptr = new ParameterizedTypeReference<OBPaymentSubmissionResponse1>() {};
            return restTemplate.exchange(resourceLink, HttpMethod.GET, new HttpEntity(headers), ptr).getBody();
        } catch (HttpClientErrorException e) {
            throw new InvalidAuthorizationCodeException(e.getResponseBodyAsString(), e);
        }
    }
}
