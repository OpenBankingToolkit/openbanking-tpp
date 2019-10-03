/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.shop.config;

import com.forgerock.openbanking.config.ApplicationConfiguration;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShopConfiguration implements ApplicationConfiguration {
    @Value("${shop.id}")
    public String id;

    @Override
    public String getIssuerID() {
        return id;
    }

    public synchronized JWKSet getJwkSet() {
        return null;
    }
}
