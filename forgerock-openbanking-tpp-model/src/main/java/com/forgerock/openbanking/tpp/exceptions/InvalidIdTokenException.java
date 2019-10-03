/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.exceptions;

public class InvalidIdTokenException extends Exception {
    private String message;
    private Integer code;

    public InvalidIdTokenException(Throwable cause) {
        super(cause);
    }

    public InvalidIdTokenException(String message, Integer code) {

        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getErrorCode() {
        return code;
    }
}
