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
