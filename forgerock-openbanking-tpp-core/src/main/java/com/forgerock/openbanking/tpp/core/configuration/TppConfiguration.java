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
package com.forgerock.openbanking.tpp.core.configuration;

import com.forgerock.openbanking.ssl.config.SslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Configuration
public class TppConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TppConfiguration.class);

    @Autowired
    private SslConfiguration sslConfiguration;

    @Value("${tpp.certificate.transport.alias}")
    private String tppTransportCertificateAlias;

    @Value("${tpp.software-id}")
    private String softwareId;

    @Value("${tpp.issuerid}")
    public String issuerId;

    @Value("${tpp.aisp.redirect_uri}")
    public String aispRedirectUri;

    @Value("${tpp.aisp.cookie.domain}")
    public String aispContextCookieDomain;

    @Value("${tpp.pisp.redirect_uri}")
    public String pispRedirectUri;

    @Value("${tpp.openbanking.version}")
    public String version;

    @Value("${tpp.ssa}")
    private Resource ssa;

    private String ssaContent = null;

    public String getSoftwareId() {
        return softwareId;
    }

    public String getAispRedirectUri() {
        return aispRedirectUri;
    }

    public String getPispRedirectUri() {
        return pispRedirectUri;
    }

    public String getAispContextCookieDomain() {
        return aispContextCookieDomain;
    }

    public String getVersion() {
        return version;
    }

    public String getSsa() {
        if (ssaContent == null) {
            StringBuilder result = new StringBuilder();

            InputStream is = null;
            BufferedReader br = null;
            try {
                is = ssa.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                LOGGER.error("Can't read SSA resource", e);
                throw new RuntimeException(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        LOGGER.error("Can't close inputStream correctly", e);
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        LOGGER.error("Can't close BufferedReader correctly", e);
                    }
                }
            }

            ssaContent = result.toString();
        }
        return ssaContent;
    }
}
