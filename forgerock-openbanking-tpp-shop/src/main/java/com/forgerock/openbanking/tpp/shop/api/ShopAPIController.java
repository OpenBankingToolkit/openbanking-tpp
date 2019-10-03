/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
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
