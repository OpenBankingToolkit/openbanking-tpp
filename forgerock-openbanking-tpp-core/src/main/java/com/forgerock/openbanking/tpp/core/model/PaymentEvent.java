/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.model;

import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import uk.org.openbanking.datamodel.payment.OBTransactionIndividualStatus1Code;

@ToString
@EqualsAndHashCode
public class PaymentEvent {

    @Id
    private String id;
    private OBTransactionIndividualStatus1Code status;
    private Long timestamp;
    private String paymentId;
    private String paymentSubmissionId;
    private FRPaymentRequest1 paymentRequest;

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
}
