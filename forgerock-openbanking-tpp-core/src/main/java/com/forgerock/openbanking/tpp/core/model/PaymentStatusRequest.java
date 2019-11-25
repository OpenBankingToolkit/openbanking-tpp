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
