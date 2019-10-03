/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
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
