package com.bankcore.common.exception;

import org.springframework.http.HttpStatus;

public class AccountFrozenException extends BusinessException {
    public AccountFrozenException(String message) {
        super(HttpStatus.BAD_REQUEST, "ACCOUNT_FROZEN", message);
    }
}
