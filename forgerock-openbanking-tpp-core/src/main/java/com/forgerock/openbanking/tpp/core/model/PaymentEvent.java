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
