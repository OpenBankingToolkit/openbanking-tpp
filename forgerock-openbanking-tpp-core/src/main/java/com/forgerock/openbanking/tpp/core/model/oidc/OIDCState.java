/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
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
