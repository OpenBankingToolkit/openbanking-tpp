/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.model;

import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import org.springframework.data.annotation.Id;
import uk.org.openbanking.datamodel.payment.OBTransactionIndividualStatus1Code;

public class PaymentStatusRequest {
    @Id
    private String paymentSubmissionId;
    private FRPaymentRequest1 paymentRequest;
    private String aspspId;
    private OBTransactionIndividualStatus1Code status;

    public String getPaymentSubmissionId() {
        return paymentSubmissionId;
    }

    public FRPaymentRequest1 getPaymentRequest() {
        return paymentRequest;
    }

    public String getAspspId() {
        return aspspId;
    }

    public OBTransactionIndividualStatus1Code getStatus() {
        return status;
    }


    public PaymentStatusRequest paymentSubmissionId(String paymentSubmissionId) {
        this.paymentSubmissionId = paymentSubmissionId;
        return this;
    }

    public PaymentStatusRequest aspspId(String aspspId) {
        this.aspspId = aspspId;
        return this;
    }

    public PaymentStatusRequest paymentRequest(FRPaymentRequest1 paymentRequest) {
        this.paymentRequest = paymentRequest;
        return this;
    }

    public PaymentStatusRequest status(OBTransactionIndividualStatus1Code status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "PaymentStatusRequest{" +
                "paymentSubmissionId='" + paymentSubmissionId + '\'' +
                ", paymentRequest=" + paymentRequest +
                ", aspspId='" + aspspId + '\'' +
                ", status=" + status +
                '}';
    }
}
