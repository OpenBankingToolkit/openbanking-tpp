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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.discovery.OBDiscoveryResponse;

import java.net.URI;

@Service
public class RSDiscoveryService {
    private final static Logger LOGGER = LoggerFactory.getLogger(RSDiscoveryService.class);
    @Autowired
    private RestTemplate restTemplate;

    public OBDiscoveryResponse discovery(String rsDiscoveryEndpoint) {
        LOGGER.debug("Call the discovery of the RS {}", rsDiscoveryEndpoint);

        ParameterizedTypeReference<OBDiscoveryResponse> ptr = new ParameterizedTypeReference<OBDiscoveryResponse>() {};
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(rsDiscoveryEndpoint);
        URI uri = builder.build().encode().toUri();

        ResponseEntity<OBDiscoveryResponse> entity = restTemplate.exchange(
                uri, HttpMethod.GET, null, ptr);
        return entity.getBody();
    }
}
