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
package com.forgerock.openbanking.tpp.core.services.aspsp.as;

import com.forgerock.openbanking.jwt.exceptions.InvalidTokenException;
import com.forgerock.openbanking.jwt.services.CryptoApiClient;
import com.forgerock.openbanking.model.claim.Claim;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.model.oidc.OIDCRegistrationResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.exceptions.InvalidAuthorizationCodeException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.openbanking.constants.OIDCConstants.*;
import static com.forgerock.openbanking.constants.OIDCConstants.OIDCClaim.OB_ACR_CA_VALUE;
import static com.forgerock.openbanking.constants.OIDCConstants.OIDCClaim.OB_ACR_SCA_VALUE;
import static com.forgerock.openbanking.constants.OpenBankingConstants.IdTokenClaim;
import static com.forgerock.openbanking.constants.OpenBankingConstants.JWT_BEARER_CLIENT_ASSERTION_TYPE;

/**
 * OIDC flows contains the functions needed by an OpenID client.
 */
@Service
public class AspspAsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AspspAsService.class);

    @Autowired
    private CryptoApiClient cryptoApiClient;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Exchange a code into an access token
     *
     * @param code an authorization code
     * @return an access token response
     * @throws InvalidAuthorizationCodeException error that could happen in the exchange flow, like an invalid code
     *                                           for example.
     */
    public AccessTokenResponse exchangeCode(String code, AspspConfiguration aspspConfiguration, String redirectUri)
            throws InvalidAuthorizationCodeException {
        String clientAuthenticationJwt = generateClientAuthenticationJwt(aspspConfiguration);

        //Request body
        LOGGER.debug("We exchange the code '{}' for an access token.", code);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(OIDCClaim.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.type);
        params.set(OIDCClaim.CODE, code);
        params.set(OIDCClaim.REDIRECT_URI, redirectUri);

        LOGGER.debug("We authenticate to the AS via the client authentication JWT");
        /* we authenticate to the AS via the client authentication JWT.*/
        params.set(OIDCClaim.CLIENT_ASSERTION_TYPE, JWT_BEARER_CLIENT_ASSERTION_TYPE);
        params.set(OIDCClaim.CLIENT_ASSERTION, clientAuthenticationJwt);
        LOGGER.debug("Client credential JWT : '{}'",clientAuthenticationJwt);

        //Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //Send request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        LOGGER.debug("Send new authorization code request to OpenAM.");

        try {
            return restTemplate.postForObject(aspspConfiguration.getOidcDiscoveryResponse().getTokenEndpoint(),
                    request, AccessTokenResponse.class);
        } catch (HttpClientErrorException e) {
            LOGGER.debug("Could not exchange the code: {}", e.getResponseBodyAsString(), e);
            throw new InvalidAuthorizationCodeException(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * Get an access token via the client credential flow
     *
     * @return an access token reponse
     */
    public AccessTokenResponse clientCredential(AspspConfiguration aspspConfiguration, List<String> scopes)
            throws InvalidTokenException {
        String clientAuthenticationJwt = generateClientAuthenticationJwt(aspspConfiguration);
        //Request body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(OIDCClaim.GRANT_TYPE, GrantType.CLIENT_CREDENTIAL.type);
        params.set(OIDCClaim.SCOPE, scopes.stream().collect(Collectors.joining(" ")));

        LOGGER.debug("We authenticate to the AS via the client authentication JWT");
        params.set(OIDCClaim.CLIENT_ASSERTION_TYPE, JWT_BEARER_CLIENT_ASSERTION_TYPE);
        params.set(OIDCClaim.CLIENT_ASSERTION, clientAuthenticationJwt);
        LOGGER.debug("Client credential JWT : '{}'", clientAuthenticationJwt);

        //Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //Send request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
                headers);
        try {
            return restTemplate.postForObject(aspspConfiguration.getOidcDiscoveryResponse().getTokenEndpoint(),
                    request, AccessTokenResponse.class);
        } catch (HttpClientErrorException e) {
            throw new InvalidTokenException(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * Generate a new request parameter. Only needed when the signature and encryption keys needs to be rotated.
     *
     * @return the request parameter JWT, containing some claims.
     * @throws JOSEException
     */
    public String generateRequestParameter(AspspConfiguration aspspConfiguration, String intentId, String state,
                                           String nonce, String redirectUri, List<String> scopes) throws JOSEException {
        //Span are needed for doing a Zipkin tracing.
        LOGGER.debug("Generate a request parameter for the intent ID '{}', state '{}' and nonce '{}'", intentId,
                state, nonce);
        OIDCRegistrationResponse oidcRegistrationResponse = aspspConfiguration.getOidcRegistrationResponse();

        JWTClaimsSet.Builder requestParameterClaims;
        requestParameterClaims = new JWTClaimsSet.Builder();
        requestParameterClaims.audience(aspspConfiguration.getOidcDiscoveryResponse().getIssuer());
        requestParameterClaims.expirationTime(new Date(new Date().getTime() + Duration.ofHours(1).toMillis()));
        requestParameterClaims.claim(OIDCClaim.RESPONSE_TYPE, ResponseType.CODE + " " + ResponseType.ID_TOKEN);
        requestParameterClaims.claim(OIDCClaim.CLIENT_ID, oidcRegistrationResponse.getClientId());
        requestParameterClaims.claim(OIDCClaim.REDIRECT_URI, redirectUri);
        requestParameterClaims.claim(OIDCClaim.SCOPE, scopes.stream().collect(Collectors.joining(" ")));
        requestParameterClaims.claim(OIDCClaim.STATE, state);
        requestParameterClaims.claim(OIDCClaim.NONCE, nonce);

        //We will ask some claims and will do a policy enforcement by using the acr essential claim.
        JSONObject claims = new JSONObject();
        JSONObject idTokenClaims = new JSONObject();
        List<String> acrValues = new ArrayList<>();
        acrValues.add(OB_ACR_CA_VALUE);
        acrValues.add(OB_ACR_SCA_VALUE);
        idTokenClaims.put(IdTokenClaim.ACR, new Claim(true, acrValues).toJson());
        idTokenClaims.put(IdTokenClaim.INTENT_ID,
                new Claim(true, intentId).toJson());
        claims.put(OIDCClaim.ID_TOKEN, idTokenClaims);
        JSONObject userInfoClaims = new JSONObject();
        userInfoClaims.put(IdTokenClaim.INTENT_ID,
                new Claim(true, intentId).toJson());
        claims.put(OIDCClaim.USER_INFO, userInfoClaims);
        requestParameterClaims.claim(OIDCClaim.CLAIMS, claims);
        LOGGER.debug("Request parameter JWS : '{}'",
                cryptoApiClient.signClaims(oidcRegistrationResponse.getClientId(), requestParameterClaims.build(), false));

        return cryptoApiClient.signAndEncryptClaims(oidcRegistrationResponse.getClientId(),
                requestParameterClaims.build(), aspspConfiguration.getOidcDiscoveryResponse().getJwksUri());
    }

    /**
     * Get an access token via the client credential flow
     *
     * @return the redirect uri
     */
    public String hybridFlow(AspspConfiguration aspspConfiguration, String state, String nonce, String requestParameter,
                             String redirectUri, List<String> scopes) {
        LOGGER.debug("Start the hybrid flow by redirecting the user to the authorize endpoint");
        //Request body
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(aspspConfiguration.getOidcDiscoveryResponse().getAuthorizationEndpoint());

        builder.queryParam(OIDCClaim.RESPONSE_TYPE, ResponseType.CODE + " " + ResponseType.ID_TOKEN);
        builder.queryParam(OIDCClaim.CLIENT_ID, aspspConfiguration.getOidcRegistrationResponse().getClientId());
        builder.queryParam(OIDCClaim.STATE, state);
        builder.queryParam(OIDCClaim.NONCE, nonce);
        builder.queryParam(OIDCClaim.SCOPE, scopes.stream().collect(Collectors.joining(" ")));
        builder.queryParam(OIDCClaim.REDIRECT_URI, redirectUri);
        builder.queryParam(OIDCClaim.REQUEST, requestParameter);

        return builder.build().encode().toUriString();
    }

    /**
     * Generate a new client authentication JWT.
     *
     * @return a JWT that can be used to authenticate Kyle to the AS.
     */
    public String generateClientAuthenticationJwt(AspspConfiguration aspspConfiguration) {
        String clientId = aspspConfiguration.getOidcRegistrationResponse().getClientId();
        JWTClaimsSet.Builder requestParameterClaims;
        requestParameterClaims = new JWTClaimsSet.Builder();
        //By putting the issuer id as subject, this JWT will play the role of client credential. Never generate
        // another JWT with client id as subject! Otherwise this JWT can be used as credential as well!
        requestParameterClaims.subject(clientId);
        requestParameterClaims.audience(aspspConfiguration.getOidcDiscoveryResponse().getIssuer());
        requestParameterClaims.expirationTime(new Date(new Date().getTime() + Duration.ofMinutes(10).toMillis()));
        return cryptoApiClient.signClaims(clientId, requestParameterClaims.build(), false);
    }
}
