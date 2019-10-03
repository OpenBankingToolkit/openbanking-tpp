/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.shop.pisp.store;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * A service for storing state->paymentID
 */
@Service
public class StatePaymentIDMemoryStore {

    private Map<String, String> statePaymentID = new HashMap<>();

    public void store(String state, String paymentID) {
        statePaymentID.put(state, paymentID);
    }

    public String retrievePaymentID(String state) {
        return statePaymentID.get(state);
    }


}
