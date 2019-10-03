/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.account.web;

import com.forgerock.openbanking.tpp.service.TppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/*")
public class ClientHTMLEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHTMLEndpoint.class);

    @Autowired
    private TppService tppService;
    @Value("${account.success-redirect-uri}")
    private String successRedirectUri;
    @Value("${account.failure-redirect-uri}")
    private String failureRedirectUri;
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }

   @GetMapping("/failure")
    public String error(
            @RequestParam(value = "message", required = true) String message,
            @RequestParam(value = "code", required = false) String code,
            Model model) {
        model.addAttribute("message", message);
        model.addAttribute("code", code);
        return "error";
    }

    @GetMapping("/selectBank")
    public String selectBank(Model model) {
        model.addAttribute("banks", tppService.getBanks());
        return "selectBank";
    }

    @GetMapping("initiateAccountRequest/{bankid}")
    public RedirectView initiatePayment(
            @PathVariable(value = "bankid") String bankid,
            HttpServletRequest request
    ) {
        try {
            return new RedirectView(tppService.initiateAccountRequest(bankid,
                    successRedirectUri + bankid + "/",
                    failureRedirectUri));
        } catch (HttpClientErrorException e) {
            return new RedirectView(failureRedirectUri + "?code=" + e.getRawStatusCode()
                    + "&message=" + e.getStatusText()
            );
        }
    }
}
