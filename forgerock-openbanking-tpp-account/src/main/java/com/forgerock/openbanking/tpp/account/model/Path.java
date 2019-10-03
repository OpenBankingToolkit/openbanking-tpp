/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.account.model;

import java.util.LinkedList;
import java.util.List;

public class Path {

    private LinkedList<PathItem> pathItems = new LinkedList<>();

    public List<PathItem> getPathItems() {
        return pathItems;
    }

    public Path addItem(PathItem item) {
        pathItems.add(item);
        return this;
    }

    public Path getCopy() {
        Path copy = new Path();
        copy.pathItems = new LinkedList<>(this.pathItems);
        return copy;
    }

    public String getPath() {
        return pathItems.getLast().getUri();

    }
}
