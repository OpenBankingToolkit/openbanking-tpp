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
package com.forgerock.openbanking.tpp.config;

import com.forgerock.openbanking.config.ApplicationConfiguration;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TPPConfiguration implements ApplicationConfiguration {
    @Value("${tpp.issuerid}")
    public String issuerId;

    @Value("${tpp.core.endpoint.getbanks}")
    public String endPointGetBanks;
    @Value("${tpp.core.endpoint.initiateAccountRequest}")
    public String endPointInitiateAccountRequest;
    @Value("${tpp.core.endpoint.exchange_code}")
    public String endpointExchangeCode;
    @Value("${tpp.core.endpoint.accounts}")
    public String endPointAccounts;
    @Value("${tpp.core.endpoint.account}")
    public String endPointAccount;
    @Value("${tpp.core.endpoint.balances}")
    public String endPointBalances;
    @Value("${tpp.core.endpoint.beneficiaries}")
    public String endPointBeneficiaries;
    @Value("${tpp.core.endpoint.directdebits}")
    public String endPointDirectDebits;
    @Value("${tpp.core.endpoint.products}")
    public String endPointProducts;
    @Value("${tpp.core.endpoint.standingOrders}")
    public String endPointStandingOrders;
    @Value("${tpp.core.endpoint.transactions}")
    public String endPointTransactions;
    @Value("${tpp.core.endpoint.initiatePaymentRequest}")
    public String endPointInitiatePaymentRequest;

    @Override
    public String getIssuerID() {
        return issuerId;
    }

    public synchronized JWKSet getJwkSet() {
        return null;
    }
}
