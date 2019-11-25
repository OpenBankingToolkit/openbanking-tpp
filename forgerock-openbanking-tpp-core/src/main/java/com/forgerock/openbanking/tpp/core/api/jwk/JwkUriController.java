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
