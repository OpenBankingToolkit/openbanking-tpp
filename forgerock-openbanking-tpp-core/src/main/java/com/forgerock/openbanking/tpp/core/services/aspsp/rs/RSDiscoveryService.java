/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
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
