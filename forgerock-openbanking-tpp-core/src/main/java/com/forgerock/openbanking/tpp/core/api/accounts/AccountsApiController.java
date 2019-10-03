/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.api.accounts;

import com.forgerock.openbanking.model.error.ErrorResponseTmp;
import com.forgerock.openbanking.model.error.ResponseCode;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.configuration.RSConfiguration;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.services.AISPContextService;
import com.forgerock.openbanking.tpp.error.ErrorHandler;
import com.nimbusds.jose.JOSEException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.OBHeaders;
import uk.org.openbanking.datamodel.account.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.openbanking.constants.OpenBankingConstants.BOOKED_TIME_DATE_FORMAT;
import static com.forgerock.openbanking.constants.OpenBankingConstants.ParametersFieldName.FROM_BOOKING_DATE_TIME;
import static com.forgerock.openbanking.constants.OpenBankingConstants.ParametersFieldName.TO_BOOKING_DATE_TIME;

@Controller
public class AccountsApiController implements AccountsApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsApiController.class);
    private final static DateTimeFormatter format = org.joda.time.format.DateTimeFormat.forPattern(BOOKED_TIME_DATE_FORMAT);

    @Autowired
    private RSConfiguration rsConfiguration;
    @Autowired
    private ErrorHandler errorHandler;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AISPContextService aispContextService;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;

    @Override
    public ResponseEntity getAccount(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue("aispContext") String aispContext) {
        try {
            LOGGER.debug("Read the account {} for aisp context {}", accountId, aispContext);
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context token.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadAccount1> ptr = new ParameterizedTypeReference<OBReadAccount1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccount());

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccounts(
            @PathVariable("bankId") String bankId,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read accounts for aisp context {}", aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadAccount1> ptr = new ParameterizedTypeReference<OBReadAccount1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();

            return restTemplate.exchange(aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccounts(),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccountBalances(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read the balances for account {} for aisp context {}", accountId, aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadBalance1> ptr = new ParameterizedTypeReference<OBReadBalance1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccountBalances());

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccountBeneficiaries(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read the beneficiaries for account {} for aisp context {}", accountId, aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadBeneficiary1> ptr = new ParameterizedTypeReference<OBReadBeneficiary1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccountBeneficiaries());

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccountDirectDebits(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read the direct debits for account {} for aisp context {}", accountId, aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadDirectDebit1> ptr = new ParameterizedTypeReference<OBReadDirectDebit1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccountDirectDebits());

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccountProduct(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read the product for account {} for aisp context {}", accountId, aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadProduct1> ptr = new ParameterizedTypeReference<OBReadProduct1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccountProduct());

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccountStandingOrders(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read the standing orders for account {} for aisp context {}", accountId, aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadStandingOrder1> ptr = new ParameterizedTypeReference<OBReadStandingOrder1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccountStandingOrders());

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }

    @Override
    public ResponseEntity getAccountTransactions(
            @PathVariable("bankId") String bankId,
            @PathVariable("AccountId") String accountId,
            @RequestParam(value = FROM_BOOKING_DATE_TIME, required = false)
            @DateTimeFormat(pattern = BOOKED_TIME_DATE_FORMAT) DateTime fromBookingDateTime,
            @RequestParam(value = TO_BOOKING_DATE_TIME, required = false)
            @DateTimeFormat(pattern = BOOKED_TIME_DATE_FORMAT) DateTime toBookingDateTime,
            @CookieValue("aispContext") String aispContext) {
        LOGGER.debug("Read the transactions for account {} for aisp context {}", accountId, aispContext);
        try {
            Optional<String> accessTokenOptional = aispContextService.accessToken(aispContext, bankId);
            if (!accessTokenOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponseTmp
                                        .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                        .message("Invalid aisp context format.")
                        );
            }
            String accessToken = accessTokenOptional.get();

            String uid = UUID.randomUUID().toString();
            //Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(OBHeaders.AUTHORIZATION, "Bearer " + accessToken);
            //It's optional and can probably be replaced by a JWS content instead.
            //headers.add(OBHeaders.X_JWS_SIGNATURE, "");
            headers.add(OBHeaders.X_FAPI_FINANCIAL_ID, rsConfiguration.financialId);
            //We don't have the user last logged time
            //headers.add(OBHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, "");
            headers.add(OBHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, "");
            headers.add(OBHeaders.X_FAPI_INTERACTION_ID, uid);
            headers.add(OBHeaders.ACCEPT, "application/json");

            ParameterizedTypeReference<OBReadTransaction1> ptr = new ParameterizedTypeReference<OBReadTransaction1>() {};

            AspspConfiguration aspspConfiguration = aspspConfigurationRepository.findById(bankId).get();
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(aspspConfiguration.getDiscoveryAPILinksAccount().getGetAccountTransactions());

            if (fromBookingDateTime != null) {
                builder.queryParam(FROM_BOOKING_DATE_TIME,  format.print(fromBookingDateTime));
            }
            if (toBookingDateTime != null) {
                builder.queryParam(TO_BOOKING_DATE_TIME,  format.print(toBookingDateTime));
            }

            return restTemplate.exchange(builder.build(Collections.singletonMap("AccountId", accountId)),
                    HttpMethod.GET, new HttpEntity(headers), ptr);
        } catch (HttpClientErrorException e) {
            LOGGER.error("error from the ASPSP-RS", e);
            return ResponseEntity.status(e.getStatusCode()).body(errorHandler.load(e.getResponseBodyAsString()));
        } catch (ParseException | JOSEException e) {
            LOGGER.error("An issue happened when creating the aisp context", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponseTmp
                                    .code(ResponseCode.ErrorCode.INVALID_AISP_CONTEXT)
                                    .message("Invalid aisp context format.")
                    );
        }
    }
}
