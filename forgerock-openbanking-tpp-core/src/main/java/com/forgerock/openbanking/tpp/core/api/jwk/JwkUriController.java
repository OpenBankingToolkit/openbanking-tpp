/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.api.jwk;

import com.forgerock.openbanking.core.services.ApplicationApiClient;
import com.forgerock.openbanking.tpp.core.configuration.TppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * TPP rest endpoint
 */
@Controller
@RequestMapping("/open-banking/v1.1/jwks/")
public class JwkUriController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwkUriController.class);

    @Autowired
    private ApplicationApiClient applicationApiClient;
    @Autowired
    private TppConfiguration tppConfiguration;

    /**
     * The JWKs uri
     *
     * @return the JWKs
     */
    @RequestMapping(value = "jwk_uri", method = RequestMethod.GET)
    public ResponseEntity<String> jwksUri() {
        LOGGER.debug("JwksUri has been called.");
        return new ResponseEntity<>(applicationApiClient.signingEncryptionKeysJwkUri(tppConfiguration.issuerId), HttpStatus.OK);
    }
}
