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
package com.forgerock.openbanking.tpp.core.model.directory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Aspsp {
    @Id
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("logo_uri")
    private String logoUri;
    @JsonProperty("financial_id")
    private String financialId;
    @JsonProperty("as_discovery_endpoint")
    private String asDiscoveryEndpoint;
    @JsonProperty("rs_discovery_endpoint")
    private String rsDiscoveryEndpoint;
    @JsonProperty("test_mtls_endpoint")
    private String testMtlsEndpoint;
    @JsonProperty("transport_keys")
    private String transportKeys;
}
