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
package com.forgerock.openbanking.tpp.core.api.event;

import com.forgerock.openbanking.exceptions.OBErrorResponseException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.org.openbanking.OBHeaders;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Api(value = "event-notifications", description = "the event notifications API")
@RequestMapping("/open-banking/v3.0/event-notifications")
public interface EventNotificationsApi {

    @ApiOperation(value = "Create an event notification", nickname = "createEventNotification", notes = "", response = void.class, authorizations = {
    }, tags = {"Event Notification"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Event Notification Accepted", response = void.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})
    @RequestMapping(
            value = "",
            consumes = {"application/jwt; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity createEventNotification(
            @ApiParam(value = "Default", required = true)
            @RequestBody String jwtSerialised,

            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = true)
            @RequestHeader(value = OBHeaders.X_FAPI_FINANCIAL_ID, required = false) String xFapiFinancialId,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = OBHeaders.X_FAPI_INTERACTION_ID, required = false) String xFapiInteractionId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;

}
