/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CORS configuration
 */
@Configuration
public class CorsConfiguration {

    @Autowired
    private TppClientsConfiguration tppClientsConfiguration;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        List<String> origins = tppClientsConfiguration.getClients().stream().map(c -> c.cors).collect(Collectors.toList());
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(origins.toArray(new String[]{}));
            }
        };
    }
}

