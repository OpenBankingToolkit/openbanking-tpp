/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.model;

import java.util.List;

public class PaymentEventsNotification {

    public List<PaymentEvent> paymentEvents;

    public PaymentEventsNotification() {}

    public PaymentEventsNotification(List<PaymentEvent> paymentEvents) {
        this.paymentEvents = paymentEvents;
    }
}
