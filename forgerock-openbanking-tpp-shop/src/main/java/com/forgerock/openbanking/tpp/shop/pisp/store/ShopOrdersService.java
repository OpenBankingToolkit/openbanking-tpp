/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
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
