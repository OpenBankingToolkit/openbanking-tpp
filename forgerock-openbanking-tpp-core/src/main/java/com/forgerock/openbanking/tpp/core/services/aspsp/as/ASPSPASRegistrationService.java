/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.services.aspsp.as;

import com.forgerock.openbanking.model.oidc.OIDCRegistrationResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ASPSPASRegistrationService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ASPSPASRegistrationService.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;

    public OIDCRegistrationResponse register(String registrationEndpoint, String registrationRequest) throws HttpClientErrorException {
        LOGGER.info("Register a new TPP to the ASPSP-AS");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/jwt"));
        //Send request
        HttpEntity<String> request = new HttpEntity<>(registrationRequest, headers);
        LOGGER.debug("Send registration request to the ASPSP-AS {}", registrationEndpoint);
        return restTemplate.postForObject(registrationEndpoint, request, OIDCRegistrationResponse.class);
    }

    public void unregister(AspspConfiguration aspspConfiguration) throws HttpClientErrorException {
        unregister(aspspConfiguration.getRegistrationEndpoint());
        aspspConfigurationRepository.delete(aspspConfiguration);
    }

    public void unregister(String registrationEndpoint) throws HttpClientErrorException {
        LOGGER.info("Unregister the TPP to the ASPSP-AS");
        try {
            restTemplate.delete(registrationEndpoint);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND
                    && e.getStatusCode() != HttpStatus.FORBIDDEN) {
                throw e;
            }
        }
    }
}
