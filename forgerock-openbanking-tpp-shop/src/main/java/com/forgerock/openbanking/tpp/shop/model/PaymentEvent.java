/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.shop.model;

import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import uk.org.openbanking.datamodel.payment.OBTransactionIndividualStatus1Code;

public class PaymentEvent {

    private String id;
    private OBTransactionIndividualStatus1Code status;
    private Long timestamp;
    private String paymentId;
    private String paymentSubmissionId;
    private FRPaymentRequest1 paymentRequest;
    private boolean notified = false;

    public PaymentEvent() {
        timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public OBTransactionIndividualStatus1Code getStatus() {
        return status;
    }

    public FRPaymentRequest1 getPaymentRequest() {
        return paymentRequest;
    }


    public Long getTimestamp() {
        return timestamp;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentSubmissionId() {
        return paymentSubmissionId;
    }

    public boolean isNotified() {
        return notified;
    }

    public PaymentEvent status(OBTransactionIndividualStatus1Code status) {
        this.status = status;
        return this;
    }

    public PaymentEvent timestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public PaymentEvent paymentId(String paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    public PaymentEvent paymentSubmissionId(String paymentSubmissionId) {
        this.paymentSubmissionId = paymentSubmissionId;
        return this;
    }

    public PaymentEvent paymentRequest(FRPaymentRequest1 paymentRequest) {
        this.paymentRequest = paymentRequest;
        return this;
    }

    public PaymentEvent notified() {
        this.notified = true;
        return this;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", paymentId='" + paymentId + '\'' +
                ", paymentSubmissionId='" + paymentSubmissionId + '\'' +
                ", paymentRequest=" + paymentRequest +
                ", notified=" + notified +
                '}';
    }
}
