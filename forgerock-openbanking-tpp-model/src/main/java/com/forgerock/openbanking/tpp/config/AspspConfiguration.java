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

import com.forgerock.openbanking.model.oidc.OIDCRegistrationResponse;
import com.forgerock.openbanking.tpp.model.openbanking.discovery.OIDCDiscoveryResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.org.openbanking.datamodel.discovery.OBDiscoveryAPILinksAccount1;
import uk.org.openbanking.datamodel.discovery.OBDiscoveryAPILinksPayment1;

@Document
public class AspspConfiguration {

    @Id
    @Indexed
    private String id;
    private String name;
    private String logo;
    private String financialId;
    private String ssa;
    private String wellKnownEndpoint;
    private String discoveryEndpoint;
    private String registrationEndpoint;
    private OIDCRegistrationResponse oidcRegistrationResponse;
    private OIDCDiscoveryResponse oidcDiscoveryResponse;
    private OBDiscoveryAPILinksPayment1 discoveryAPILinksPayment;
    private OBDiscoveryAPILinksAccount1 discoveryAPILinksAccount;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public String getFinancialId() {
        return financialId;
    }

    public void setFinancialId(String financialId) {
        this.financialId = financialId;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSsa() {
        return ssa;
    }

    public void setSsa(String ssa) {
        this.ssa = ssa;
    }

    public String getWellKnownEndpoint() {
        return wellKnownEndpoint;
    }

    public void setWellKnownEndpoint(String wellKnownEndpoint) {
        this.wellKnownEndpoint = wellKnownEndpoint;
    }

    public String getDiscoveryEndpoint() {
        return discoveryEndpoint;
    }

    public void setDiscoveryEndpoint(String discoveryEndpoint) {
        this.discoveryEndpoint = discoveryEndpoint;
    }

    public OIDCRegistrationResponse getOidcRegistrationResponse() {
        return oidcRegistrationResponse;
    }

    public void setOidcRegistrationResponse(OIDCRegistrationResponse oidcRegistrationResponse) {
        this.oidcRegistrationResponse = oidcRegistrationResponse;
    }

    public OIDCDiscoveryResponse getOidcDiscoveryResponse() {
        return oidcDiscoveryResponse;
    }

    public void setOidcDiscoveryResponse(OIDCDiscoveryResponse oidcDiscoveryResponse) {
        this.oidcDiscoveryResponse = oidcDiscoveryResponse;
    }

    public OBDiscoveryAPILinksPayment1 getDiscoveryAPILinksPayment() {
        return discoveryAPILinksPayment;
    }

    public OBDiscoveryAPILinksAccount1 getDiscoveryAPILinksAccount() {
        return discoveryAPILinksAccount;
    }

    public void setDiscoveryAPILinksPayment(OBDiscoveryAPILinksPayment1 discoveryAPILinksPayment) {
        this.discoveryAPILinksPayment = discoveryAPILinksPayment;
    }

    public void setDiscoveryAPILinksAccount(OBDiscoveryAPILinksAccount1 discoveryAPILinksAccount) {
        this.discoveryAPILinksAccount = discoveryAPILinksAccount;
    }

    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }

    public void setRegistrationEndpoint(String registrationEndpoint) {
        this.registrationEndpoint = registrationEndpoint;
    }
}
