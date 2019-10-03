/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
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
