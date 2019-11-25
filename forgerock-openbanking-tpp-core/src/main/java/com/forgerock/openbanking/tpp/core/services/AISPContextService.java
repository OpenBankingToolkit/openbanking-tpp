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
package com.forgerock.openbanking.tpp.core.services;

import com.forgerock.openbanking.constants.OpenBankingConstants;
import com.forgerock.openbanking.jwt.services.CryptoApiClient;
import com.forgerock.openbanking.tpp.core.configuration.TppConfiguration;
import com.forgerock.openbanking.tpp.core.model.AISPSessionContext;
import com.forgerock.openbanking.tpp.core.model.ASPSPContext;
import com.forgerock.openbanking.tpp.core.repository.AispContextRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
public class AISPContextService {

    @Autowired
    private TppConfiguration tppConfiguration;
    @Autowired
    private CryptoApiClient cryptoApiClient;
    @Autowired
    private AispContextRepository aispContextRepository;

    public Optional<String> accessToken(String aispContext, String aspspId) throws ParseException, JOSEException {
        AISPSessionContext aispSessionContext = getASPSPSessionContext(aispContext);
        if (aispContext == null || aispSessionContext.getAspspContext().get(aspspId) == null) {
            return Optional.empty();
        }
        return Optional.of(aispSessionContext.getAspspContext().get(aspspId).getAccessToken());
    }

    /**
     * Generate a new client authentication JWT.
     *
     * @return a JWT that can be used to authenticate Kyle to the AS.
     */
    public String generateAISPContextJwt(String aispContext, String accountRequestId, String aspspId, String accessToken) throws ParseException, JOSEException {
        AISPSessionContext aispSessionContext = getASPSPSessionContext(aispContext);
        ASPSPContext aspspContext = new ASPSPContext();
        aspspContext.setAspspId(aspspId);
        aspspContext.setIntentId(accountRequestId);
        aspspContext.setAccessToken(accessToken);
        aispSessionContext.getAspspContext().put(aspspId, aspspContext);
        aispSessionContext = aispContextRepository.save(aispSessionContext);

        JWTClaimsSet.Builder requestParameterClaims;
        requestParameterClaims = new JWTClaimsSet.Builder();
        requestParameterClaims.issuer(tppConfiguration.issuerId);
        requestParameterClaims.audience(tppConfiguration.issuerId);
        requestParameterClaims.expirationTime(new Date(new Date().getTime() + Duration.ofDays(7).toMillis()));
        requestParameterClaims.claim(OpenBankingConstants.AISPContextClaims.ASPSP_SESSION_CONTEXT, aispSessionContext.getId());
        return cryptoApiClient.signAndEncryptJwtForOBApp(requestParameterClaims.build(), tppConfiguration.issuerId);
    }

    private AISPSessionContext getASPSPSessionContext(String aispContext) throws ParseException, JOSEException {
        if (aispContext == null) {
            return new AISPSessionContext();
        }
        SignedJWT aispContextJws = cryptoApiClient.decryptJwe(tppConfiguration.issuerId, aispContext);
        String contextSerialized = aispContextJws.getJWTClaimsSet().getStringClaim(OpenBankingConstants.AISPContextClaims.ASPSP_SESSION_CONTEXT);
        return aispContextRepository.findById(contextSerialized).get();

    }
}
