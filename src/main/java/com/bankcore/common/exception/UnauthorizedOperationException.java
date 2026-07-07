package com.bankcore.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedOperationException extends BusinessException {
    public UnauthorizedOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "UNAUTHORIZED_OPERATION", message);
    }
}
