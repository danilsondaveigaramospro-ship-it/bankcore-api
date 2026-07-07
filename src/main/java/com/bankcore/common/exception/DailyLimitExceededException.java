package com.bankcore.common.exception;

import org.springframework.http.HttpStatus;

public class DailyLimitExceededException extends BusinessException {
    public DailyLimitExceededException(String message) {
        super(HttpStatus.BAD_REQUEST, "DAILY_LIMIT_EXCEEDED", message);
    }
}
