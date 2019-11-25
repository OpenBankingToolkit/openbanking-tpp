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
package com.forgerock.openbanking.tpp.core.model.oidc;

import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class OIDCState {

    @Id
    @Indexed
    private String state;
    @Indexed
    private String intentId;
    @Indexed
    private String aspspId;
    private String onFailureRedirectUri;
    private String onSuccessRedirectUri;
    private FRPaymentRequest1 paymentRequest;

    public String getState() {
        return state;
    }

    public String getIntentId() {
        return intentId;
    }

    public String getAspspId() {
        return aspspId;
    }

    public String getOnSuccessRedirectUri() {
        return onSuccessRedirectUri;
    }

    public String getOnFailureRedirectUri() {
        return onFailureRedirectUri;
    }

    public FRPaymentRequest1 getPaymentRequest() {
        return paymentRequest;
    }

    public OIDCState intentId(String intentId) {
        this.intentId = intentId;
        return this;
    }

    public OIDCState aspspId(String aspspId) {
        this.aspspId = aspspId;
        return this;
    }

    public OIDCState onFailureRedirectUri(String onFailureRedirectUri) {
        this.onFailureRedirectUri = onFailureRedirectUri;
        return this;
    }

    public OIDCState onSuccessRedirectUri(String onSuccessRedirectUri) {
        this.onSuccessRedirectUri = onSuccessRedirectUri;
        return this;
    }

    public OIDCState paymentRequest(FRPaymentRequest1 paymentRequest) {
        this.paymentRequest = paymentRequest;
        return this;
    }

}
