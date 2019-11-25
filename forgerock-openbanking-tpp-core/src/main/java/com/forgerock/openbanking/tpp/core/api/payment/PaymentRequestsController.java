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
package com.forgerock.openbanking.tpp.core.api.payment;

import com.forgerock.openbanking.constants.OpenBankingConstants;
import com.forgerock.openbanking.jwt.exceptions.InvalidTokenException;
import com.forgerock.openbanking.jwt.services.CryptoApiClient;
import com.forgerock.openbanking.model.error.ErrorResponseTmp;
import com.forgerock.openbanking.model.error.ResponseCode;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.configuration.TppConfiguration;
import com.forgerock.openbanking.tpp.core.model.FRPaymentSetup;
import com.forgerock.openbanking.tpp.core.model.PaymentEvent;
import com.forgerock.openbanking.tpp.core.model.PaymentStatusRequest;
import com.forgerock.openbanking.tpp.core.model.oidc.OIDCState;
import com.forgerock.openbanking.tpp.core.repository.*;
import com.forgerock.openbanking.tpp.core.services.RedirectService;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.AspspAsService;
import com.forgerock.openbanking.tpp.core.services.aspsp.rs.RSPaymentAPIService;
import com.forgerock.openbanking.tpp.core.services.oidc.HybridFlowService;
import com.forgerock.openbanking.tpp.exceptions.InvalidAuthorizationCodeException;
import com.forgerock.openbanking.tpp.exceptions.InvalidIdTokenException;
import com.forgerock.openbanking.tpp.exceptions.InvalidStateException;
import com.forgerock.openbanking.tpp.exceptions.RegistrationFailure;
import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.org.openbanking.datamodel.payment.paymentsubmission.OBPaymentSubmissionResponse1;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * PISP rest endpoint
 */
@Controller
public class PaymentRequestsController implements PaymentRequests {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestsController.class);

    public static final List<String> PISP_SCOPES = Arrays.asList(
            OpenBankingConstants.Scope.ACCOUNTS,
            OpenBankingConstants.Scope.OPENID,
            OpenBankingConstants.Scope.PAYMENTS,
            OpenBankingConstants.Scope.FUNDS_CONFIRMATIONS
    );

    @Autowired
    private CryptoApiClient cryptoApiClient;
    @Autowired
    private AspspAsService aspspAsService;
    @Autowired
    private RSPaymentAPIService rsPaymentAPIService;
    @Autowired
    private TppConfiguration tppConfiguration;
    @Autowired
    private PaymentSetupRepository paymentSetupRepository;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;
    @Autowired
    private OIDCStateMongoRepository oidcStateRepository;
    @Autowired
    private HybridFlowService hybridFlowService;
    @Autowired
    private RedirectService redirectService;
    @Autowired
    private PaymentStatusRequestRepository paymentStatusRequestRepository;
    @Autowired
    private PaymentEventsRepository paymentEventsRepository;

    /**
     * The initiate payment as defined by the OpenBanking standard.
     *
     * @return redirect to the authorization endpoint of the AS, as described by the hybrid flow
     */
    public ResponseEntity initiatePayment(@RequestBody FRPaymentRequest1 paymentRequest,
                                                  @RequestParam(value = "bankId") String bankId,
                                                  @RequestParam(value = "onSuccessRedirectUri") String onSuccessRedirectUri,
                                                  @RequestParam(value = "onFailureRedirectUri") String onFailureRedirectUri
    ) {
        // We had a tag for Zipkin, so we can track request from an order ID.

        Optional<AspspConfiguration> aspspConfigurationOptional = aspspConfigurationRepository.findById(bankId);
        if (!aspspConfigurationOptional.isPresent()) {
            return ResponseEntity
                    .status( HttpStatus.NOT_FOUND)
                    .body(ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.ASPSP_CONFIG_NOT_FOUND).message("Bank ID '" + bankId + "' not found.")
                    );
        }
        AspspConfiguration aspspConfiguration = aspspConfigurationOptional.get();
        OIDCState oidcState = oidcStateRepository.save(new OIDCState()
                .aspspId(bankId)
                .onSuccessRedirectUri(onSuccessRedirectUri)
                .onFailureRedirectUri(onFailureRedirectUri)
                .paymentRequest(paymentRequest)
        );

        //Step 2 get an access token via the client grant flow
        try {
            LOGGER.debug("Get an access token via the client credential flow");
            //TODO for this demo, we generate an access token each time. It can actually be re-used until expiration.
            AccessTokenResponse accessTokenResponse = aspspAsService.clientCredential(aspspConfiguration, PISP_SCOPES);
            LOGGER.debug("Received access token '{}'", accessTokenResponse.access_token);

            LOGGER.debug("Register the payment to the RS");
            FRPaymentSetup paymentSetup = rsPaymentAPIService.registerPayment(aspspConfiguration, paymentRequest,
                    accessTokenResponse);
            paymentSetupRepository.save(paymentSetup);
            String paymentID = paymentSetup.getId();

            LOGGER.debug("Create a new payment event");
            paymentEventsRepository.save(new PaymentEvent()
                    .paymentId(paymentID)
                    .paymentRequest(paymentRequest)
                    .status(paymentSetup.getStatus().toOBTransactionIndividualStatus1Code())
            );

            //We tag the payment ID to the request so we can track requests associated with a payment ID.
            LOGGER.debug("The received payment ID '{}'", paymentID);

            oidcState.intentId(paymentID);
            oidcStateRepository.save(oidcState);

            String nonce = oidcState.getState();
            //create the request parameter
            String requestParameter = aspspAsService.generateRequestParameter(aspspConfiguration, paymentID, oidcState.getState(), nonce,
                    tppConfiguration.getPispRedirectUri(), PISP_SCOPES);
            LOGGER.debug("Start the hybrid flow with the following request param '{}'", requestParameter);

            //redirect to the authorization page with the request parameter.
            return new ResponseEntity<>(aspspAsService.hybridFlow(aspspConfiguration, oidcState.getState(), nonce, requestParameter,
                    tppConfiguration.getPispRedirectUri(), PISP_SCOPES), HttpStatus.OK);
        } catch (InvalidTokenException e) {
            LOGGER.error("Invalid client authentication JWT", e);
            return ResponseEntity
                    .status( HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_CLIENT_AUTHENTICATION_JWT).message("Invalid client authentication JWT")
                    );
        } catch (RegistrationFailure | JOSEException e) {
            LOGGER.error("FRPaymentConsent registration failed", e);
            return ResponseEntity
                    .status( HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.PAYMENT_REGISTRATION_FAILED).message("Payment registration failed")
                    );
        }
    }

    /**
     * Exchange code endpoint, to complete the hybrid flow. The AS will call this endpoint via a redirection of the
     * user to it. It will send us the authorization code which will allow us to get an access token. The state and
     * the id token are here for extra validation. As the request go through the user, the OpenBanking standard
     * expects we validate the consistency of the information received with the initial request.
     *
     * @param code    the authorization code
     * @param idToken the ID token
     * @param state   the state
     */
    public void exchangeCode(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "id_token") String idToken,
            @RequestParam(value = "state") String state,
            HttpServletResponse response) {
        LOGGER.debug("Received authorization code '{}', ID token '{}' and state '{}'", code, idToken, state);
        OIDCState oidcState = oidcStateRepository.findById(state).get();
        try {
            hybridFlowService.validateIDToken(code, idToken, state);
            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(oidcState.getAspspId()).get();

            LOGGER.debug("Exchange the code '{}' to an access token.", code);
            AccessTokenResponse accessTokenResponse = aspspAsService.exchangeCode(code, aspspConfiguration,
                    tppConfiguration.getPispRedirectUri());
            LOGGER.debug("Received an access token '{}' in exchange of the code.", accessTokenResponse.access_token);

            //Validate signatures
            LOGGER.debug("Validate the access token signature {}.", accessTokenResponse.access_token);
            cryptoApiClient.verifyAccessToken(accessTokenResponse.getAccessTokenJWT().serialize());
            Optional<FRPaymentSetup> paymentSetupOptional = paymentSetupRepository.findById(oidcState.getIntentId());
            if (!paymentSetupOptional.isPresent()) {
                LOGGER.error("Invalid state: can't retrieve payment request from payment ID {}", oidcState.getIntentId());
                redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                        ErrorResponseTmp.code(ResponseCode.ErrorCode.INVALID_STATE).message("Can't retrieve initial payment request from state"));
                return;
            }
            FRPaymentSetup paymentSetup = paymentSetupOptional.get();

            LOGGER.debug("Send a payment submission to RS for payment id '{}'", oidcState.getIntentId());
            OBPaymentSubmissionResponse1 paymentResponse = rsPaymentAPIService.paymentSubmission(aspspConfiguration,
                    paymentSetup, oidcState.getIntentId(), accessTokenResponse);

            LOGGER.debug("Create a new pulling for this submission ID");
            PaymentStatusRequest paymentStatusRequest = new PaymentStatusRequest()
                    .aspspId(aspspConfiguration.getId())
                    .paymentSubmissionId(paymentResponse.getData().getPaymentSubmissionId())
                    .paymentRequest(oidcState.getPaymentRequest())
                    .status(paymentResponse.getData().getStatus());
            paymentStatusRequestRepository.save(paymentStatusRequest);

            LOGGER.debug("Create a new payment event");
            paymentEventsRepository.save(new PaymentEvent()
                    .paymentSubmissionId(paymentResponse.getData().getPaymentSubmissionId())
                    .paymentId(paymentResponse.getData().getPaymentId())
                    .paymentRequest(oidcState.getPaymentRequest())
                    .status(paymentResponse.getData().getStatus())
            );

            LOGGER.debug("Save this payment submission ID {} for pulling status later  on", paymentStatusRequest);

            LOGGER.debug("Show the basket again with the payment response '{}'", paymentResponse);
            redirectService.successRedirect(response, oidcState.getOnSuccessRedirectUri());
        } catch (InvalidAuthorizationCodeException e) {
            LOGGER.error("The authorization code is invalid", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_AUTHORIZATION_CODE)
                            .message("The authorization code is invalid"));
        } catch (ParseException | IOException e) {
            LOGGER.error("An issue happened when parsing the access token", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_ACCESS_TOKEN)
                            .message("Invalid access token format."));
        } catch (InvalidTokenException e) {
            LOGGER.error("Invalid access token", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_ACCESS_TOKEN)
                            .message("Invalid access token."));
        }  catch (InvalidIdTokenException e) {
            LOGGER.error("Invalid ID token", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_ID_TOKEN)
                            .message("Invalid ID token"));
        }  catch (InvalidStateException e) {
            LOGGER.error("Invalid state", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_STATE)
                            .message("Invalid state"));
        }
    }
}
