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
package com.forgerock.openbanking.tpp.core.services;

import com.forgerock.openbanking.tpp.core.configuration.TppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service("TppCookieService") // Required to avoid clash with commons CookieService
public class CookieService {

    public static final String AISP_CONTEXT_COOKIE_NAME = "aisp_context";

    @Autowired
    private TppConfiguration tppConfiguration;

    public void addAISPContextCookie(HttpServletResponse response, String aispContenxt) {
        Cookie cookie = new Cookie(CookieService.AISP_CONTEXT_COOKIE_NAME, aispContenxt);
        cookie.setPath("/");
        cookie.setDomain(tppConfiguration.getAispContextCookieDomain());
        response.addCookie(cookie);
    }
}
