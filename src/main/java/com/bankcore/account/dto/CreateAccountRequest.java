package com.bankcore.account.dto;

import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountRequest(
        @NotNull UUID customerId,
        @NotNull CurrencyCode currency,
        @NotNull AccountType accountType,
        @NotNull @DecimalMin(value = "0.00") BigDecimal dailyTransferLimit
) {
}
