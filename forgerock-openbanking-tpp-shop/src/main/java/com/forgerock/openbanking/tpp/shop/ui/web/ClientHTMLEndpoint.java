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
package com.forgerock.openbanking.tpp.shop.ui.web;

import com.forgerock.openbanking.tpp.service.TppService;
import com.forgerock.openbanking.tpp.shop.model.shop.ShopOrder;
import com.forgerock.openbanking.tpp.shop.pisp.PISPService;
import com.forgerock.openbanking.tpp.shop.pisp.store.ShopOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping("/*")
public class ClientHTMLEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHTMLEndpoint.class);

    @Autowired
    private ShopOrdersService shopOrdersService;
    @Autowired
    private PISPService pispService;
    @Autowired
    private TppService tppService;
    @Value("${shop.success-redirect-uri}")
    private String successRedirectUri;
    @Value("${shop.failure-redirect-uri}")
    private String failureRedirectUri;

    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/basket")
    public String basket(Model model) {
        //TODO for the moment, we only have one order available.
        model.addAttribute("banks", tppService.getBanks());
        model.addAttribute("order", shopOrdersService.getOrder("3"));
        return "basket";
    }

    @GetMapping("/success")
    public String success(Model model) {
        model.addAttribute("success", "Success! Your payment has been accepted.");
        return basket(model);
    }

    @GetMapping("initiatePayment")
    public RedirectView initiatePayment(
            @RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "bankId") String bankId,
            HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d",request.getScheme(),  request.getServerName(), request.getServerPort());
        try {
            return new RedirectView(pispService.initiatePayment(orderId, bankId,
                    successRedirectUri,
                    failureRedirectUri + "?orderId=" + orderId));
        } catch (HttpClientErrorException e) {
            log.error("Failed to initiate payment", e);
            return new RedirectView(failureRedirectUri + "?code=" + e.getRawStatusCode()
                    + "&message=" + e.getStatusText()
                    + "&orderId=" + orderId
            );
        }
    }

    @GetMapping("/failure")
    public String error(
            @RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "message", required = true) String message,
            @RequestParam(value = "code", required = false) String code,
            Model model) {
        ShopOrder order = shopOrdersService.getOrder(orderId);
        model.addAttribute("order", order);
        model.addAttribute("errorCode", code);
        model.addAttribute("errorDescription", message);
        LOGGER.error("Received the following error for order id '{}'. Error code = '{}' and description = '{}'",
                order, code, message);
        return basket(model);
    }
}
