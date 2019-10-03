/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.account.web;


import com.forgerock.openbanking.tpp.account.model.Path;
import com.forgerock.openbanking.tpp.account.model.PathItem;
import com.forgerock.openbanking.tpp.account.service.CookieService;
import com.forgerock.openbanking.tpp.service.TppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.org.openbanking.datamodel.account.OBReadAccount1;

@Controller
public class BankEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankEndpoint.class);

    @Autowired
    private TppService tppService;

    public Path getGlobalPath(String bankId) {
        return new Path().addItem(new PathItem("Bank " + bankId, "/bank/" + bankId));
    }

    @GetMapping("/bank/{BankId}/")
    public String home(
            @PathVariable("BankId") String bankId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {
        return accounts(bankId, aispContextJws, model);
    }

    @GetMapping("/bank/{BankId}/accounts")
    public String accounts(
            @PathVariable("BankId") String bankId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {

        Path path = getGlobalPath(bankId);

        OBReadAccount1 obReadAccount = tppService.accounts(aispContextJws, bankId);
        model.addAttribute("accounts", obReadAccount.getData().getAccount());
        model.addAttribute("path", path);
        return "bank/accounts";
    }
}
