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


import com.forgerock.openbanking.model.Tpp;
import com.forgerock.openbanking.tpp.model.openbanking.ConsentStatusCode;
import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentConsent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import uk.org.openbanking.datamodel.payment.OBInitiation1;
import uk.org.openbanking.datamodel.payment.OBRisk1;
import uk.org.openbanking.datamodel.payment.paymentsetup.OBPaymentSetup1;

import java.util.Date;

/**
 * FRPaymentConsent setup entity for storing in DB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FRPaymentSetup implements FRPaymentConsent {
    @Id
    @Indexed
    public String id;
    @Indexed
    public ConsentStatusCode status;
    public OBPaymentSetup1 paymentSetupRequest;

    @Indexed
    public String accountId;
    @Indexed
    public String userId;
    @Indexed
    public String pispId;
    public String pispName;

    public DateTime created;
    @LastModifiedDate
    public Date updated;

    @Override
    public void setPisp(Tpp tpp) {
        this.pispId = tpp.getId();
        this.pispName = tpp.getOfficialName();

    }

    @Override
    public OBInitiation1 getInitiation() {
        return paymentSetupRequest.getData().getInitiation();
    }

    @Override
    public OBRisk1 getRisk() {
        return paymentSetupRequest.getRisk();
    }

}
