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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.openbanking.model.oidc.AccessTokenResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.configuration.RSConfiguration;
import com.forgerock.openbanking.tpp.exceptions.RegistrationFailure;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.org.openbanking.OBHeaders;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.account.OBReadRequest1;
import uk.org.openbanking.datamodel.account.OBReadResponse1;

import java.util.UUID;

/**
 * OIDC flows contains the functions needed by an OpenID client.
 */
@Service
public class RSAccountAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAccountAPIService.class);

    @Autowired
    private RSConfiguration rsConfiguration;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Register a new payment
     *
     * @param accessTokenResponse the access token for registering the payment
     * @return the payment response from the RS
     * @throws RegistrationFailure
     */
    public OBReadResponse1 createAccountRequest(AspspConfiguration aspspConfiguration, AccessTokenResponse accessTokenResponse)
            throws RegistrationFailure {
        LOGGER.debug("Create an account request");
        OBReadData1 dataSetup = new OBReadData1()
                .addPermissionsItem(OBExternalPermissions1Code.READACCOUNTSDETAIL)
                .addPermissionsItem(OBExternalPermissions1Code.READBALANCES)
                .addPermissionsItem(OBExternalPermissions1Code.READBENEFICIARIESDETAIL)
                .addPermissionsItem(OBExternalPermissions1Code.READDIRECTDEBITS)
                .addPermissionsItem(OBExternalPermissions1Code.READPRODUCTS)
                .addPermissionsItem(OBExternalPermissions1Code.READSTANDINGORDERSDETAIL)
                .addPermissionsItem(OBExternalPermissions1Code.READTRANSACTIONSCREDITS)
                .addPermissionsItem(OBExternalPermissions1Code.READTRANSACTIONSDEBITS)
                .addPermissionsItem(OBExternalPermissions1Code.READTRANSACTIONSDETAIL)
                .expirationDateTime(DateTime.now().withDurationAdded(Duration.standardDays(1).getMillis(), 1))
                .transactionFromDateTime(DateTime.now().withDurationAdded(Duration.standardDays(200).getMillis(), -1))
                .transactionToDateTime(DateTime.now());

        OBReadRequest1 accountRequest = new OBReadRequest1()
                .data(dataSetup);

        String uid = UUID.randomUUID().toString();
        //Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessTokenResponse.access_token);
        //It's optional and can probably be replaced by a JWS content instead.
        //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
        headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
        //We don't have the user last logged time
        //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
        headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
        headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
        headers.add(OBHeaders.ACCEPT, "application/json");

        //Send request
        HttpEntity<OBReadRequest1> request = new HttpEntity<>(accountRequest, headers);

        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug("FRAccount1 request: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString
                        (request));
            } catch (JsonProcessingException e) {
                LOGGER.error("Could not print request", e);
            }
        }
        try {
            return restTemplate.postForObject(aspspConfiguration.getDiscoveryAPILinksAccount().getCreateAccountRequest(), request, OBReadResponse1.class);
        } catch (HttpClientErrorException e) {
            LOGGER.error("Could not register payment to RS", e);
            throw new RegistrationFailure(e.getResponseBodyAsString(), e);
        }
    }
}
