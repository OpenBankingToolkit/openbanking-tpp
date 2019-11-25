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
package com.forgerock.openbanking.tpp.core.services.oidc;


import com.forgerock.openbanking.constants.OIDCConstants;
import com.forgerock.openbanking.constants.OpenBankingConstants;
import com.forgerock.openbanking.jwt.exceptions.InvalidTokenException;
import com.forgerock.openbanking.jwt.services.CryptoApiClient;
import com.forgerock.openbanking.model.error.ResponseCode;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.model.oidc.OIDCState;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.repository.OIDCStateMongoRepository;
import com.forgerock.openbanking.tpp.exceptions.InvalidIdTokenException;
import com.forgerock.openbanking.tpp.exceptions.InvalidStateException;
import com.forgerock.openbanking.tpp.utils.HashUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.forgerock.openbanking.constants.OIDCConstants.OIDCClaim.OB_ACR_CA_VALUE;
import static com.forgerock.openbanking.constants.OIDCConstants.OIDCClaim.OB_ACR_SCA_VALUE;

@Service
public class HybridFlowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HybridFlowService.class);

    @Autowired
    private CryptoApiClient cryptoApiClient;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;
    @Autowired
    private OIDCStateMongoRepository oidcStateRepository;
    /**
     * Decrypt the ID token received with our key
     *
     * @param idToken the id token encrypted, in a string format
     * @return the id token decrypted, in the JWS format
     * @throws ParseException
     * @throws InvalidTokenException
     */
    public SignedJWT decryptIDToken(String idToken) throws ParseException, JOSEException {
        SignedJWT signedIdToken = cryptoApiClient.decryptJwe(idToken);
        LOGGER.debug("Received id_token decrypted '{}'", signedIdToken.serialize());
        return signedIdToken;
    }

    public void validateIDToken(String code,String idToken, String state)
            throws InvalidIdTokenException, InvalidStateException {
        try {
            Optional<OIDCState> oidcStateOptional = oidcStateRepository.findById(state);
            //read payment id from the state
            if (!oidcStateOptional.isPresent()) {
                LOGGER.error("Invalid state");
                throw new InvalidStateException("State '" + state + "' is invalid.");
            }
            OIDCState oidcState = oidcStateOptional.get();
            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(oidcState.getAspspId()).get();

            cryptoApiClient.validateJws(idToken, aspspConfiguration.getOidcDiscoveryResponse().getIssuer(), aspspConfiguration.getOidcDiscoveryResponse().getJwksUri());
            SignedJWT signedIdToken = SignedJWT.parse(idToken);

            validateACR(signedIdToken);
            validateCHash(signedIdToken, code);
            validateSHash(signedIdToken, state);

            //read payment id from the state
            String intentIDFromInitialRequest = oidcState.getIntentId();
            LOGGER.debug("The initial payment ID behind the following state '{}' is '{}'",
                    state, intentIDFromInitialRequest);

            validateIntentIDsFromInitialRequest(signedIdToken, intentIDFromInitialRequest);
        }  catch (ParseException | InvalidIdTokenException | InvalidTokenException | IOException e) {
            LOGGER.error("Invalid ID token", e);
            throw new InvalidIdTokenException(e);
        }
    }

    /**
     * Validate the ACR Authentication Context class reference. We want to make sure that the user used the right
     * chain to authenticate.
     *
     * @param signedIdToken the ID token
     * @throws ParseException
     * @throws InvalidIdTokenException
     */
    public void validateACR(SignedJWT signedIdToken) throws ParseException, InvalidIdTokenException {
        List<String> acrValues = new ArrayList<>();
        acrValues.add(OB_ACR_CA_VALUE);
        acrValues.add(OB_ACR_SCA_VALUE);
        LOGGER.debug("Verify the acr claim of the id token matches '{}' or '{}'", OIDCConstants.OIDCClaim.OB_ACR_CA_VALUE, OIDCConstants.OIDCClaim.OB_ACR_SCA_VALUE);
        String acrInIdToken = signedIdToken.getJWTClaimsSet().getStringClaim(OpenBankingConstants.IdTokenClaim.ACR);
        if (!acrValues.contains(acrInIdToken)) {
            LOGGER.error("The acr in the id token '{}' is not equal to the expected acr '{}' or '{}'",
                    acrInIdToken,  OIDCConstants.OIDCClaim.OB_ACR_CA_VALUE, OIDCConstants.OIDCClaim.OB_ACR_SCA_VALUE);
            throw new InvalidIdTokenException("Invalid chain used during authentication.",
                    ResponseCode.ErrorCode.INVALID_ACCESS_TOKEN_RESPONSE);
        }
    }

    /**
     * Validate the authorization code matches with the code hash in the ID token
     * (The ID token will contain a claim called c_hash, which is the hash of the code).
     * As the ID token is signed, by validating the consistency of the code with the ID token, we make sure that the
     * code is valid.
     *
     * @param signedIdToken the ID token
     * @param code          the authorization code
     * @throws ParseException
     * @throws InvalidIdTokenException
     */
    public void validateCHash(SignedJWT signedIdToken, String code) throws ParseException, InvalidIdTokenException {
        //validate the authorization code using the c_hash
        String cHashFromCode = HashUtils.computeHash(code);
        LOGGER.debug("Verify the c_hash matches '{}'", cHashFromCode);
        String cHashFromIdToken = (String) signedIdToken.getJWTClaimsSet().getClaim(OpenBankingConstants.IdTokenClaim.C_HASH);
        if (!cHashFromCode.equals(cHashFromIdToken + "==")) {
            LOGGER.error("The c_hash in the id token '{}' is not equal to the expected c_hash '{}' computed from " +
                    "the code '{}'.", cHashFromIdToken, cHashFromCode, code);
            throw new InvalidIdTokenException("The c_hash received in the id_token doesn't match the code.",
                    ResponseCode.ErrorCode.INVALID_C_HASH);
        }
    }

    /**
     * Validate the state matches with the state hash in the ID token
     * (The ID token will contain a claim called s_hash, which is the hash of the code).
     * As the ID token is signed, by validating the consistency of the state with the ID token, we make sure that the
     * state is valid.
     *
     * @param signedIdToken the ID token
     * @param state         the state
     * @throws ParseException
     * @throws InvalidIdTokenException
     */
    public void validateSHash(SignedJWT signedIdToken, String state) throws ParseException, InvalidIdTokenException {
        //validate the state using the s_hash
        String sHashFromState = HashUtils.computeHash(state);
        LOGGER.debug("Verify the s_hash matches '{}'", sHashFromState);
        String sHashFromIdToken = (String) signedIdToken.getJWTClaimsSet().getClaim(OpenBankingConstants.IdTokenClaim.S_HASH);
        if (!sHashFromState.equals(sHashFromIdToken + "==")) {
            LOGGER.error("The s_hash in the id token '{}' is not equal to the expected s_hash '{}' computed from " +
                            "the state '{}'.",
                    sHashFromIdToken, sHashFromState, state);
            throw new InvalidIdTokenException("The s_hash received in the id_token doesn't match the state.",
                    ResponseCode.ErrorCode.INVALID_S_HASH);
        }
    }

    /**
     * Validate the intent ID in the ID token matches the expected intent ID. When we did the hybrid flow,
     * we specified the intent ID corresponding to this request. We want to make sure that when we exchange the code,
     * the intent ID is still the same.
     *
     * @param signedIdToken                the ID token
     * @param intentIdFromInitialRequest the intent ID we specified in the hybrid flow request.
     * @throws ParseException
     * @throws InvalidIdTokenException
     */
    public void validateIntentIDsFromInitialRequest(SignedJWT signedIdToken, String intentIdFromInitialRequest)
            throws ParseException, InvalidIdTokenException {
        //validate intent id from reading the intent id claim in the id token
        //TODO we need to customise the id token subject, to return the payment ID and not the user uid
        //String paymentIdsFromIdTokenClaims = signedIdToken.getJWTClaimsSet().getSubject();
        //For the moment, we read it from the claim, which is more openid spirit IMHO
        String intentIdFromIdTokenClaims = (String) signedIdToken.getJWTClaimsSet().getClaim
                (OpenBankingConstants.IdTokenClaim.INTENT_ID);
        LOGGER.debug("Read the intent id from the id token claim", intentIdFromIdTokenClaims);
        LOGGER.debug("Verify that the initial intent id that we associated with the state is the same than the " +
                "one received in the id token claim.");

        if (!intentIdFromIdTokenClaims.equals(intentIdFromInitialRequest)) {
            LOGGER.error("The intent id that we initially registered '{}' is not equal to the intent id " +
                            "received in the id token claims '{}'.",
                    intentIdFromInitialRequest, intentIdFromInitialRequest);
            throw new InvalidIdTokenException("intent ID retrieve from ID token is not equal to the initial intent " +
                    "ID send in the request parameter.",
                    ResponseCode.ErrorCode.INVALID_INTENT_ID);
        }
    }
}
