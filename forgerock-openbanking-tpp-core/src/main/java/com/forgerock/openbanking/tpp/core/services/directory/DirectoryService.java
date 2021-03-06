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
package com.forgerock.openbanking.tpp.core.services.directory;


import com.forgerock.openbanking.core.model.Application;
import com.forgerock.openbanking.model.ApplicationIdentity;
import com.forgerock.openbanking.model.SoftwareStatement;
import com.forgerock.openbanking.tpp.core.configuration.DirectoryConfiguration;
import com.forgerock.openbanking.tpp.core.model.directory.Aspsp;
import com.forgerock.openbanking.tpp.core.model.directory.Organisation;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.JSONObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Access the Jwk MS services
 */
@Service
@Slf4j
public class DirectoryService {

    private DirectoryConfiguration directoryConfiguration;
    private RestTemplate restTemplate;

    @Autowired
    public DirectoryService(DirectoryConfiguration directoryConfiguration, RestTemplate restTemplate) {
        this.directoryConfiguration = directoryConfiguration;
        this.restTemplate = restTemplate;
    }

    public ApplicationIdentity authenticate(JWK jwk) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<ApplicationIdentity> ptr = new ParameterizedTypeReference<ApplicationIdentity>() {};
        HttpEntity<String> request = new HttpEntity<>(JSONObjectUtils.toJSONString(jwk.toJSONObject()), headers);

        ResponseEntity<ApplicationIdentity> entity = restTemplate.exchange(directoryConfiguration.authenticateEndpoint,
                HttpMethod.POST, request, ptr);

        return entity.getBody();
    }

    public List<SoftwareStatement> getSoftwareStatements(String organisationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<List<SoftwareStatement>> ptr = new ParameterizedTypeReference<List<SoftwareStatement>>() {};
        HttpEntity<String> request = new HttpEntity<>(headers);

       return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/organisation/" + organisationId + "/software-statements",
                HttpMethod.GET, request, ptr).getBody();
    }

    public void deleteSoftwareStatements(String organisationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<List<SoftwareStatement>> ptr = new ParameterizedTypeReference<List<SoftwareStatement>>() {};
        HttpEntity<String> request = new HttpEntity<>(headers);

        restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/organisation/" + organisationId + "/software-statements",
                HttpMethod.DELETE, request, ptr).getBody();
    }

    public SoftwareStatement getCurrentSoftwareStatement() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<SoftwareStatement> ptr = new ParameterizedTypeReference<SoftwareStatement>() {};
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/current",
                HttpMethod.GET, request, ptr).getBody();
    }

    public SoftwareStatement createSoftwareStatement(SoftwareStatement softwareStatement) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        ParameterizedTypeReference<SoftwareStatement> ptr = new ParameterizedTypeReference<SoftwareStatement>() {};
        HttpEntity<SoftwareStatement> request = new HttpEntity<>(softwareStatement, headers);

        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/",
                HttpMethod.POST, request, ptr).getBody();
    }

    public SoftwareStatement updateSoftwareStatement(SoftwareStatement softwareStatement) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        ParameterizedTypeReference<SoftwareStatement> ptr = new ParameterizedTypeReference<SoftwareStatement>() {};
        HttpEntity<SoftwareStatement> request = new HttpEntity<>(softwareStatement, headers);

        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/" + softwareStatement.getId(),
                HttpMethod.PUT, request, ptr).getBody();
    }

    public String generateSSA(SoftwareStatement softwareStatement) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<String> ptr = new ParameterizedTypeReference<String>() {};
        HttpEntity request = new HttpEntity<>(headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/"
                        + softwareStatement.getId() + "/ssa",
                HttpMethod.POST, request, ptr).getBody();
    }

    public boolean deleteSoftwareStatement(String softwareStatementId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        ParameterizedTypeReference<SoftwareStatement> ptr = new ParameterizedTypeReference<SoftwareStatement>() {};
        HttpEntity<SoftwareStatement> request = new HttpEntity<>(headers);

        restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/" + softwareStatementId,
                HttpMethod.DELETE, request, ptr);
        return true;
    }

    public String getCurrentTransportPem(String softwareStatementId, String kid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<String> ptr = new ParameterizedTypeReference<String>() {};
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/" + softwareStatementId
                        + "/application/" + kid + "/download/publicCert",
                HttpMethod.GET, request, ptr).getBody();
    }

    public Application getApplication(String softwareStatementId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<Application> ptr = new ParameterizedTypeReference<Application>() {};
        HttpEntity request = new HttpEntity<>(headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/software-statement/" + softwareStatementId
                        + "/application",
                HttpMethod.GET, request, ptr).getBody();
    }

    public String login(String idToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Id-Token", idToken);

        ParameterizedTypeReference<String> ptr = new ParameterizedTypeReference<String>() {};
        HttpEntity request = new HttpEntity<>(headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/user/login",
                HttpMethod.GET, request, ptr).getBody();
    }

    public List<Aspsp> getAPSPSPs() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<List<Aspsp>> ptr = new ParameterizedTypeReference<List<Aspsp>>() {};
        HttpEntity request = new HttpEntity<>(headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/aspsp/",
                HttpMethod.GET, request, ptr).getBody();
    }

    public Organisation getOrganisation(String organisationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<Organisation> ptr = new ParameterizedTypeReference<Organisation>() {};
        HttpEntity request = new HttpEntity<>(headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/organisation/" + organisationId,
                HttpMethod.GET, request, ptr).getBody();
    }

    public Organisation updateOrganisation(Organisation organisation) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ParameterizedTypeReference<Organisation> ptr = new ParameterizedTypeReference<Organisation>() {};
        HttpEntity<Organisation> request = new HttpEntity<>(organisation, headers);
        return restTemplate.exchange(directoryConfiguration.rootEndpoint + "/api/organisation/" + organisation.getId(),
                HttpMethod.PUT, request, ptr).getBody();
    }
}
