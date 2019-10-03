/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.shop.model.shop;

import java.util.List;

public class ShopOrder {

    private String id;
    private String user;
    private List<ShopItem> shopItemList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<ShopItem> getShopItemList() {
        return shopItemList;
    }

    public void setShopItemList(List<ShopItem> shopItemList) {
        this.shopItemList = shopItemList;
    }

    public double total() {
        return shopItemList.stream().mapToDouble(ShopItem::getPrice).sum();
    }

    @Override
    public String toString() {
        return "ShopOrder{" +
                "id='" + id + '\'' +
                ", user='" + user + '\'' +
                ", shopItemList=" + shopItemList +
                '}';
    }
}
