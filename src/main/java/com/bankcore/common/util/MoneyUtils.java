package com.bankcore.common.util;

import com.bankcore.common.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Amount is required");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal requirePositive(BigDecimal amount) {
        BigDecimal normalized = normalize(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        return normalized;
    }
}
