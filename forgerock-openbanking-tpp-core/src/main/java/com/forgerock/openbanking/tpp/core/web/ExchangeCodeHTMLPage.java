/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/open-banking/v1.1")
public class ExchangeCodeHTMLPage {

    private final String TPP_EXCHANGE_CODE_URI = "tppExchangeCodeUri";

    @GetMapping("/exchange_code/aisp")
    public String aispExchangeCode(Model model) {
        model.addAttribute(TPP_EXCHANGE_CODE_URI, "/open-banking/v1.1/accountrequests/exchange_code");
        return "exchangeCode";
    }

    @GetMapping("/exchange_code/pisp")
    public String pispExchangeCode(Model model) {
        model.addAttribute(TPP_EXCHANGE_CODE_URI, "/open-banking/v1.1/payments/exchange_code");
        return "exchangeCode";
    }
}
