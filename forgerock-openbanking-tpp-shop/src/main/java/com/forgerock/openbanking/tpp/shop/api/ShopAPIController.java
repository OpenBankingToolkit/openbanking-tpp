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
package com.forgerock.openbanking.tpp.shop.api;

import com.forgerock.openbanking.tpp.shop.model.PaymentEventsNotification;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class ShopAPIController implements ShopApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShopApi.class);

    @Override
    public ResponseEntity statusCallback(
            @ApiParam(value = "FRPaymentConsent submissions status", required = true)
            @Valid
            @RequestBody PaymentEventsNotification paymentEventsNotification
    ) {
        LOGGER.debug("Received payment events notification '{}'", paymentEventsNotification);
        //TODO it's the way to get feedback on the payment completion. For now, the shop is too dummy
        return ResponseEntity.ok().build();
    }
}
