package com.bankcore.common.exception;

import org.springframework.http.HttpStatus;

public class AccountClosedException extends BusinessException {
    public AccountClosedException(String message) {
        super(HttpStatus.BAD_REQUEST, "ACCOUNT_CLOSED", message);
    }
}
