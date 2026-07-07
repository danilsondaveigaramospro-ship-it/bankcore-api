package com.bankcore.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidCurrencyException extends BusinessException {
    public InvalidCurrencyException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY", message);
    }
}
