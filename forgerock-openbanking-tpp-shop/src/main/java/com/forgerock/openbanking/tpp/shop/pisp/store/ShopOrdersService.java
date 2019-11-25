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
package com.forgerock.openbanking.tpp.shop.pisp.store;


import com.forgerock.openbanking.tpp.shop.model.shop.ShopItem;
import com.forgerock.openbanking.tpp.shop.model.shop.ShopOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ShopOrdersService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShopOrdersService.class);


    /**
     * Get the shop order
     *
     * @param id the order ID
     * @return the shop order
     */
    public ShopOrder getOrder(String id) {
        LOGGER.debug("Read the shop order '{}' from the shop store.", id);
        ShopItem tshirt = new ShopItem();
        tshirt.setName("Black T-shirt");
        tshirt.setImageSrc("/images/items/tshirt.jpg");
        tshirt.setPrice(15.0);

        ShopItem guitar = new ShopItem();
        guitar.setName("Cyan Guitars Zodiac Baritone");
        guitar.setImageSrc("/images/items/guitar.jpg");
        guitar.setPrice(1500.0);

        ShopOrder shopOrder1 = new ShopOrder();
        shopOrder1.setId("order1");
        shopOrder1.setUser("demo");
        shopOrder1.setShopItemList(Stream.of(tshirt, guitar).collect(Collectors.toList()));

        return shopOrder1;
    }
}
