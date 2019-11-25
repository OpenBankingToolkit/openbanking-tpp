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
package com.forgerock.openbanking.tpp.core.api.accounts;


import com.forgerock.openbanking.constants.OpenBankingConstants;
import com.forgerock.openbanking.jwt.exceptions.InvalidTokenException;
import com.forgerock.openbanking.jwt.services.CryptoApiClient;
import com.forgerock.openbanking.model.error.ErrorResponseTmp;
import com.forgerock.openbanking.model.error.ResponseCode;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.configuration.TppConfiguration;
import com.forgerock.openbanking.tpp.core.model.oidc.OIDCState;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.repository.OIDCStateMongoRepository;
import com.forgerock.openbanking.tpp.core.services.AISPContextService;
import com.forgerock.openbanking.tpp.core.services.CookieService;
import com.forgerock.openbanking.tpp.core.services.RedirectService;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.AspspAsService;
import com.forgerock.openbanking.tpp.core.services.aspsp.rs.RSAccountAPIService;
import com.forgerock.openbanking.tpp.core.services.oidc.HybridFlowService;
import com.forgerock.openbanking.tpp.exceptions.InvalidAuthorizationCodeException;
import com.forgerock.openbanking.tpp.exceptions.InvalidIdTokenException;
import com.forgerock.openbanking.tpp.exceptions.InvalidStateException;
import com.forgerock.openbanking.tpp.exceptions.RegistrationFailure;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import uk.org.openbanking.datamodel.account.OBReadResponse1;

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
public class AccountRequestsController implements AccountRequests {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRequestsController.class);

    private static final List<String> AISP_SCOPES = Arrays.asList(OpenBankingConstants.Scope.ACCOUNTS,
            OpenBankingConstants.Scope.OPENID);

    @Autowired
    private CryptoApiClient cryptoApiClient;
    @Autowired
    private OIDCStateMongoRepository oidcStateRepository;
    @Autowired
    private AspspAsService aspspAsService;
    @Autowired
    private RSAccountAPIService rsAccountAPIService;
    @Autowired
    private AISPContextService aispContextService;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;
    @Autowired
    private TppConfiguration tppConfiguration;
    @Autowired
    private HybridFlowService hybridFlowService;
    @Autowired
    @Qualifier("TppCookieService")
    private CookieService cookieService;
    @Autowired
    private RedirectService redirectService;

    /**
     * The initiate payment as defined by the OpenBanking standard.
     *
     * @return redirect to the authorization endpoint of the AS, as described by the hybrid flow
     */
    public ResponseEntity initiateAccountRequest(
            @RequestParam(value = "bankId") String bankId,
            @RequestParam(value = "onSuccessRedirectUri") String onSuccessRedirectUri,
            @RequestParam(value = "onFailureRedirectUri") String onFailureRedirectUri
            ) {

        LOGGER.debug("Start initiateAccountRequest with bankId {}, onSuccessRedirectUri {}, onFailureRedirectUri {}",
                bankId, onSuccessRedirectUri, onFailureRedirectUri);

        Optional<AspspConfiguration> aspspConfigurationOptional = aspspConfigurationRepository.findById(bankId);
        if (!aspspConfigurationOptional.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.ASPSP_CONFIG_NOT_FOUND)
                                    .message("Bank ID '" + bankId + "' not found.")
                    );
        }
        AspspConfiguration aspspConfiguration = aspspConfigurationOptional.get();

        LOGGER.debug("We generate an OIDC state, where we save the request parameters");
        OIDCState oidcState = oidcStateRepository.save(new OIDCState()
                .aspspId(bankId)
                .onSuccessRedirectUri(onSuccessRedirectUri)
                .onFailureRedirectUri(onFailureRedirectUri)
        );

        String state = oidcState.getState();
        String nonce = state;
        LOGGER.debug("The state id is {}", state);

        //Step 2 get an access token via the client grant flow
        try {
            LOGGER.debug("Get an access token via the client credential flow");
            //TODO for this demo, we generate an access token each time. It can actually be re-used until expiration.
            AccessTokenResponse accessTokenResponse = aspspAsService.clientCredential(aspspConfiguration, AISP_SCOPES);
            LOGGER.debug("Received access token '{}'", accessTokenResponse.access_token);

            LOGGER.debug("Register the payment to the RS");
            OBReadResponse1 accountRequestResponse = rsAccountAPIService.createAccountRequest(aspspConfiguration, accessTokenResponse);
            String accountRequestID = accountRequestResponse.getData().getAccountRequestId();
            //We tag the payment ID to the request so we can track requests associated with a payment ID.
            LOGGER.debug("The received account Request ID '{}'", accountRequestID);

            oidcState.intentId(accountRequestID);
            oidcStateRepository.save(oidcState);

            //create the request parameter
            String requestParameter = aspspAsService.generateRequestParameter(aspspConfiguration, accountRequestID,
                    state, nonce, tppConfiguration.getAispRedirectUri(), AISP_SCOPES);
            LOGGER.debug("Start the hybrid flow with the following request param '{}'", requestParameter);

            //redirect to the authorization page with the request parameter.
            return new ResponseEntity<>(aspspAsService.hybridFlow(aspspConfiguration, state, nonce, requestParameter,
                    tppConfiguration.getAispRedirectUri(), AISP_SCOPES), HttpStatus.OK);
        } catch (InvalidTokenException e) {
            LOGGER.error("Invalid client authentication JWT", e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_CLIENT_AUTHENTICATION_JWT)
                                    .message("Invalid client authentication JWT")
                    );
        } catch (RegistrationFailure | JOSEException e) {
            LOGGER.error("Payment registration failed", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.PAYMENT_REGISTRATION_FAILED)
                                    .message("FRPaymentConsent registration failed")
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
            @CookieValue(value = "aispContext", required = false) String aispContext,
            HttpServletResponse response) {
        LOGGER.debug("Received authorization code '{}', ID token '{}' and state '{}'", code, idToken, state);
        OIDCState oidcState = oidcStateRepository.findById(state).get();

        try {
            hybridFlowService.validateIDToken(code, idToken, state);
            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(oidcState.getAspspId()).get();
            LOGGER.debug("Exchange the code '{}' to an access token.", code);
            AccessTokenResponse accessTokenResponse = aspspAsService.exchangeCode(code,
                    aspspConfiguration, tppConfiguration.getAispRedirectUri());
            LOGGER.debug("Received an access token '{}' in exchange of the code.", accessTokenResponse.access_token);

            //Validate signatures
            LOGGER.debug("Validate the access token signature.", accessTokenResponse.access_token);
            cryptoApiClient.verifyAccessToken(accessTokenResponse.getAccessTokenJWT().serialize());

            cookieService.addAISPContextCookie(response,
                    aispContextService.generateAISPContextJwt(aispContext, oidcState.getIntentId(),
                    oidcState.getAspspId(), accessTokenResponse.getAccessTokenJWT().serialize()));
            redirectService.successRedirect(response, oidcState.getOnSuccessRedirectUri());

        } catch (InvalidAuthorizationCodeException e) {
            LOGGER.error("The authorization code is invalid", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INVALID_AUTHORIZATION_CODE)
                            .message("The authorization code is invalid"));
        } catch (ParseException e) {
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
        } catch (HttpClientErrorException e) {
            LOGGER.error("Unexpected HTTP error {}", e.getResponseBodyAsString(), e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
                            .message("An error error on the server side. Contact your administrator"));
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
        } catch (JOSEException | IOException e) {
            LOGGER.error("Unexpected error {}", e);
            redirectService.failureRedirect(response, oidcState.getOnFailureRedirectUri(),
                    ErrorResponseTmp
                            .code(ResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
                            .message("An error error on the server side. Contact your administrator"));
        }
    }
}
