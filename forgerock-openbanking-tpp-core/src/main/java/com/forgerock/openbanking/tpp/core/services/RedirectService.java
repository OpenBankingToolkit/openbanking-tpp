/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.services;


import com.forgerock.openbanking.model.error.ErrorResponseTmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class RedirectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectService.class);

    public void successRedirect(HttpServletResponse response, String onSuccessRedirectUri) {
        try {
            response.sendRedirect(onSuccessRedirectUri);
        } catch (IOException e) {
            LOGGER.error("Invalid successful redirect uri {}", onSuccessRedirectUri, e);
        }
    }

    public void failureRedirect(HttpServletResponse response, String onFailureRedirectUri, ErrorResponseTmp errorResponseTmp)
    {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(onFailureRedirectUri);
        builder.queryParam("message", errorResponseTmp.getMessage());
        builder.queryParam("code", errorResponseTmp.getCode());
        try {
            response.sendRedirect(builder.build().encode().toUriString());
        } catch (IOException e) {
            LOGGER.error("Invalid failure redirect uri {}. The error was {}", onFailureRedirectUri, errorResponseTmp, e);
        }
    }
}
