/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties("tpp")
public class TppClientsConfiguration {

    public static class ClientsConfig {
        public String id;
        public String cors;
        public String statusCallback;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCors() {
            return cors;
        }

        public void setCors(String cors) {
            this.cors = cors;
        }

        public String getStatusCallback() {
            return statusCallback;
        }

        public void setStatusCallback(String statusCallback) {
            this.statusCallback = statusCallback;
        }
    }
    public List<ClientsConfig> clients;

    public List<ClientsConfig> getClients() {
        return clients;
    }

    public void setClients(List<ClientsConfig> clients) {
        this.clients = clients;
    }

    public Map<String, String> getStatusCallback() {
        return getClients().stream().collect(Collectors.toMap(ClientsConfig::getId, ClientsConfig::getStatusCallback));
    }
}
