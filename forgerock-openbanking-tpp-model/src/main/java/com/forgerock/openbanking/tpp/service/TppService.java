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
package com.forgerock.openbanking.tpp.service;

import com.forgerock.openbanking.tpp.config.TPPConfiguration;
import com.forgerock.openbanking.tpp.model.bank.Bank;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.account.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.forgerock.openbanking.constants.OpenBankingConstants.BOOKED_TIME_DATE_FORMAT;
import static com.forgerock.openbanking.constants.OpenBankingConstants.ParametersFieldName.FROM_BOOKING_DATE_TIME;
import static com.forgerock.openbanking.constants.OpenBankingConstants.ParametersFieldName.TO_BOOKING_DATE_TIME;

@Service
public class TppService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TppService.class);
    private static final DateTimeFormatter format = DateTimeFormat.forPattern(BOOKED_TIME_DATE_FORMAT);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TPPConfiguration tppConfiguration;

    /**
     * Initiate an account request
     *
     * @return the redirection uri to the authorization endpoint
     */
    public List<Bank> getBanks() {
        LOGGER.debug("Get banks .");
        return restTemplate.getForEntity(tppConfiguration.endPointGetBanks , List.class).getBody();
    }


    /**
     * Initiate an account request
     *
     * @return the redirection uri to the authorization endpoint
     */
    public String initiateAccountRequest(String bankid, String onSuccessRedirectUri, String onFailureRedirectUri) {
        LOGGER.debug("Initiate account request .");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("bankId", bankid);
        map.add("onSuccessRedirectUri", onSuccessRedirectUri);
        map.add("onFailureRedirectUri", onFailureRedirectUri);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        return restTemplate.exchange(tppConfiguration.endPointInitiateAccountRequest, HttpMethod.POST,  request, String.class).getBody();
    }

    /**
     * Get accounts
     *
     * @return the accounts
     */
    public OBReadAccount1 accounts(String aispContext, String bankId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointAccounts);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadAccount1.class).getBody();
    }

    /**
     * Get account
     *
     * @return the account
     */
    public OBReadAccount1 account(String aispContext, String bankId, String accountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointAccounts);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadAccount1.class).getBody();
    }

    /**
     * Get balances
     *
     * @return the balances
     */
    public OBReadBalance1 balances(String aispContext, String bankId, String accountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointBalances);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadBalance1.class).getBody();
    }

    /**
     * Get beneficiaries
     *
     * @return the beneficiaries
     */
    public OBReadBeneficiary1 beneficiaries(String aispContext, String bankId, String accountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointBeneficiaries);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadBeneficiary1.class).getBody();
    }


    /**
     * Get Direct Debits
     *
     * @return the Direct Debits
     */
    public OBReadDirectDebit1 directDebits(String aispContext, String bankId, String accountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointDirectDebits);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadDirectDebit1.class).getBody();
    }

    /**
     * Get products
     *
     * @return the products
     */
    public OBReadProduct1 products(String aispContext, String bankId, String accountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointProducts);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadProduct1.class).getBody();
    }

    /**
     * Get standing orders
     *
     * @return the standing orders
     */
    public OBReadStandingOrder1 standingOrders(String aispContext, String bankId, String accountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointStandingOrders);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);

        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadStandingOrder1.class).getBody();
    }

    /**
     * Get transactions
     *
     * @return the transactions
     */
    public OBReadTransaction1 transactions(String aispContext, String bankId, String accountId, DateTime fromBookingDateTime,
            DateTime toBookingDateTime) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointTransactions);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bankId", bankId);
        parameters.put("accountId", accountId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "aispContext=" + aispContext);
        if (fromBookingDateTime != null) {
            builder.queryParam(FROM_BOOKING_DATE_TIME,  format.print(fromBookingDateTime));
        }
        if (toBookingDateTime != null) {
            builder.queryParam(TO_BOOKING_DATE_TIME,  format.print(toBookingDateTime));
        }
        return restTemplate.exchange(builder.build(parameters), HttpMethod.GET, new HttpEntity(headers), OBReadTransaction1.class).getBody();
    }
}
