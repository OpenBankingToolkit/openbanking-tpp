/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.model;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

public class AISPSessionContext {

    @Id
    public String id;

    public String getId() {
        return id;
    }

    public Map<String, ASPSPContext> aspspContext = new HashMap<>();

    public Map<String, ASPSPContext> getAspspContext() {
        return aspspContext;
    }

}
