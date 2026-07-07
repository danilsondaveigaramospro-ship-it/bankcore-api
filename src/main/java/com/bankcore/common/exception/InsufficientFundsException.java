package com.bankcore.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends BusinessException {
    public InsufficientFundsException(String message) {
        super(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", message);
    }
}
