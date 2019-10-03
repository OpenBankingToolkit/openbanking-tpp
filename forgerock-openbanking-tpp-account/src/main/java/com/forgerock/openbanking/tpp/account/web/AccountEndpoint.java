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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import uk.org.openbanking.datamodel.account.*;

import static com.forgerock.openbanking.constants.OpenBankingConstants.ParametersFieldName.FROM_BOOKING_DATE_TIME;
import static com.forgerock.openbanking.constants.OpenBankingConstants.ParametersFieldName.TO_BOOKING_DATE_TIME;

@Controller
@RequestMapping("/bank/{BankId}/account/{AccountId}/*")
public class AccountEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEndpoint.class);

    @Autowired
    private TppService tppService;

    public Path getGlobalPath(String bankId, String accountId) {
        Path path = new Path()
                .addItem(new PathItem("Bank " + bankId, "/bank/" + bankId));
        path.addItem(new PathItem("Account " + accountId, path .getPath() + "/account/" + accountId));
        return path;
    }

    @GetMapping("/")
    public String account(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {
        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadAccount1 account = tppService.account(aispContextJws, bankId, accountId);
            model.addAttribute("account", account.getData().getAccount().get(0));
            model.addAttribute("path", path);
            return "account/account";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }

    @GetMapping("balances/")
    public String balances(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {
        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadBalance1 balances = tppService.balances(aispContextJws, bankId, accountId);
            model.addAttribute("balances", balances.getData().getBalance());
            model.addAttribute("path", path);
            return "account/balances";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }

    @GetMapping("beneficiaries/")
    public String beneficiaries(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {

        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadBeneficiary1 beneficiaries = tppService.beneficiaries(aispContextJws, bankId, accountId);
            model.addAttribute("beneficiaries", beneficiaries.getData().getBeneficiary());
            model.addAttribute("path", path);
            return "account/beneficiaries";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }

    @GetMapping("direct-debits/")
    public String directDebits(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {

        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadDirectDebit1 directDebit = tppService.directDebits(aispContextJws, bankId, accountId);
            model.addAttribute("directDebits", directDebit.getData().getDirectDebit());
            model.addAttribute("path", path);
            return "account/direct-debits";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }

    @GetMapping("products/")
    public String product(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {

        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadProduct1 products = tppService.products(aispContextJws, bankId, accountId);
            model.addAttribute("products", products.getData().getProduct());
            model.addAttribute("path", path);
            return "account/products";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }


    @GetMapping("standing-orders/")
    public String standingOrders(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {

        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadStandingOrder1 standingOrders = tppService.standingOrders(aispContextJws, bankId, accountId);
            model.addAttribute("standingOrders", standingOrders.getData().getStandingOrder());
            model.addAttribute("path", path);
            return "account/standing-orders";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }

    @GetMapping("transactions/")
    public String transactions(
            @PathVariable("BankId") String bankId,
            @PathVariable("AccountId") String accountId,

            @RequestParam(value = FROM_BOOKING_DATE_TIME, required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd-HH:mm:ss") DateTime fromBookingDateTime,

            @RequestParam(value =TO_BOOKING_DATE_TIME, required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd-HH:mm:ss")DateTime toBookingDateTime,

            @CookieValue(value = CookieService.AISP_CONTEXT_COOKIE_NAME) String aispContextJws,
            Model model) {

        try {
            Path path = getGlobalPath(bankId, accountId);

            OBReadTransaction1 transactions = tppService.transactions(aispContextJws, bankId, accountId,
                    fromBookingDateTime, toBookingDateTime);
            model.addAttribute("transactions", transactions.getData().getTransaction());
            model.addAttribute("path", path);
            return "account/transactions";
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString() != null && !"".equals(e.getResponseBodyAsString())) {
                model.addAttribute("message", e.getResponseBodyAsString());
            }
            return "error";
        }
    }
}
