/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.api.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.openbanking.tpp.core.scheduler.PaymentsStatusService;
import com.forgerock.openbanking.tpp.model.notification.EventType;
import com.forgerock.openbanking.tpp.model.notification.ResourceUpdateEvent;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.org.openbanking.OBHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Map;

import static com.forgerock.openbanking.constants.OpenBankingConstants.EventNotificationClaims.EVENTS;

@Controller
@Slf4j
public class EventNotificationsApiController implements EventNotificationsApi {

    private PaymentsStatusService paymentsStatusService;
    private ObjectMapper objectMapper;

    @Autowired
    public EventNotificationsApiController(PaymentsStatusService paymentsStatusService, ObjectMapper objectMapper) {
        this.paymentsStatusService = paymentsStatusService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity createEventNotification(
            @ApiParam(value = "Default", required = true)
            @RequestBody String jwtSerialised,

            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = true)
            @RequestHeader(value = "x-fapi-financial-id", required = false) String xFapiFinancialId,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = true) String xFapiInteractionId,

            HttpServletRequest request,

            Principal principal
    ) {
        try {
            log.debug("InteractionID:{}", xFapiInteractionId);
            log.debug("Received JWT: {}", jwtSerialised);
            final SignedJWT signedJWT = SignedJWT.parse(jwtSerialised);

            log.debug("JWT Claims for notification JWT: {}", signedJWT.getJWTClaimsSet());
            ResourceUpdateEvent resourceUpdateEvent = objectMapper.readValue(
                    getResourceEventClaimString(signedJWT.getJWTClaimsSet()),
                    ResourceUpdateEvent.class);
            log.info("Received event notification from issuer: '{}'. A '{}' with id: '{}' has updated. Link for the payment is: {}. InteractionID: {}",
                    signedJWT.getJWTClaimsSet().getIssuer(),
                    resourceUpdateEvent.getSubject().getResourceType(),
                    resourceUpdateEvent.getSubject().getResourceId(),
                    resourceUpdateEvent.getFirstResourceLink().orElse(""),
                    xFapiInteractionId
            );

            // Update saved payment request if matching to resource in event
            paymentsStatusService.updatePayment(resourceUpdateEvent);
        } catch (ParseException | IOException e) {
            log.warn("Unable to parse jwt [{}] with parse exception. InteractionId: {}", jwtSerialised, xFapiInteractionId, e);
            return ResponseEntity
                    .badRequest()
                    .header(OBHeaders.X_FAPI_INTERACTION_ID, xFapiInteractionId)
                    .body("Invalid JWT");
        }

        return ResponseEntity
                .accepted()
                .header(OBHeaders.X_FAPI_INTERACTION_ID, xFapiInteractionId)
                .build();
    }

    private String getResourceEventClaimString(JWTClaimsSet jwtClaimsSet) {
        return ((Map) jwtClaimsSet.getClaim(EVENTS))
                .get(EventType.RESOURCE_UPDATE_EVENT.getEventName())
                .toString();
    }
}
