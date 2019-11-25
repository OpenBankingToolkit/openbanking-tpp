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
package com.forgerock.openbanking.tpp.shop.pisp;

import com.forgerock.openbanking.tpp.config.TPPConfiguration;
import com.forgerock.openbanking.tpp.model.openbanking.v1_1.payment.FRPaymentRequest1;
import com.forgerock.openbanking.tpp.shop.config.ShopConfiguration;
import com.forgerock.openbanking.tpp.shop.model.shop.ShopOrder;
import com.forgerock.openbanking.tpp.shop.pisp.store.ShopOrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.payment.*;
import uk.org.openbanking.datamodel.payment.paymentsetup.OBPaymentSetup1;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PISPService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PISPService.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TPPConfiguration tppConfiguration;
    @Autowired
    private ShopOrdersService shopOrdersService;
    @Autowired
    private ShopConfiguration shopConfiguration;
    /**
     * Initiate a payment
     *
     * @param orderId the order ID
     * @return the redirection uri to the authorization endpoint
     */
    public String initiatePayment(String orderId, String bankid, String onSuccessRedirectUri, String onFailureRedirectUri) {
        LOGGER.debug("Initiate payment for order '{}'.", orderId);
        ShopOrder order = shopOrdersService.getOrder(orderId);

        OBInitiation1 initiation = generateInitiation(order);
        OBPaymentDataSetup1 dataSetup = new OBPaymentDataSetup1()
                .initiation(initiation);

        OBPaymentSetup1 paymentSetupRequest = new OBPaymentSetup1()
                .risk(this.generateRisk())
                .data(dataSetup);

        FRPaymentRequest1 paymentRequest = new FRPaymentRequest1();
        paymentRequest.setMerchantId(shopConfiguration.id);
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setPaymentSetup(paymentSetupRequest);

        HttpEntity<FRPaymentRequest1> request = new HttpEntity<>(paymentRequest, new HttpHeaders());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tppConfiguration.endPointInitiatePaymentRequest);

        builder.queryParam("bankId", bankid);
        builder.queryParam("onSuccessRedirectUri", onSuccessRedirectUri);
        builder.queryParam("onFailureRedirectUri", onFailureRedirectUri);

        return restTemplate.exchange(builder.build().toUri(), HttpMethod.POST,
                request, String.class).getBody();
    }


    private OBInitiation1 generateInitiation(ShopOrder order) {
        //TODO complete those values from the shopAccount and the transaction
        String accountNumber = "21325698";
        String sortCode = "080800";
        String accountName = "ACME Inc";
        OBExternalAccountIdentification2Code accountIdentificationCode = OBExternalAccountIdentification2Code
                .SortCodeAccountNumber;
        return new OBInitiation1()
                .instructionIdentification("ShopUI")
                .endToEndIdentification(order.getId())
                .instructedAmount(new OBActiveOrHistoricCurrencyAndAmount()
                        .amount(order.total() + "")
                        .currency("GBP"))
                .creditorAccount(new OBCashAccountCreditor1()
                        .schemeName(accountIdentificationCode)
                        .identification( sortCode + accountNumber)
                        .name(accountName)
                )
                .remittanceInformation(new OBRemittanceInformation1()
                        .reference(
                                "Order " + order.getId())
                        .unstructured(order.getId()));
    }

    private OBRisk1 generateRisk() {

        //TODO complete those values from the shopAccount and the transaction
        OBExternalPaymentContext1Code paymentContextCode = OBExternalPaymentContext1Code.ECOMMERCEGOODS;
        String merchantCategoryCode = "5967";
        String merchantCustomerIdentification = "053598653254";
        List<String> addressLines = Stream.of("Flat 7", "Acacia Lodge").collect(Collectors.toList());
        String streetName = "Acacia Avenue";
        String buildingNumber = "27";
        String postCode ="GU31 2ZZ";
        String townName = "Sparsholt";
        String countySubDivision = "Wessex";
        String country = "UK";

        return new OBRisk1()
                .paymentContextCode(paymentContextCode)
                .merchantCategoryCode(merchantCategoryCode)
                .merchantCustomerIdentification(merchantCustomerIdentification)
                .deliveryAddress(new OBRisk1DeliveryAddress()
                        .addressLine(addressLines)
                        .streetName(streetName)
                        .buildingNumber(buildingNumber)
                        .postCode(postCode)
                        .townName(townName)
                        .countrySubDivision(Arrays.asList(countySubDivision))
                        .country(country)
                );
    }

}
