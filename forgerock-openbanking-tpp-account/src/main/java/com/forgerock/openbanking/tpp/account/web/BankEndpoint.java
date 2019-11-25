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
