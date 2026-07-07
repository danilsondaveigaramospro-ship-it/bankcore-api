package com.bankcore.account.dto;

import com.bankcore.common.enums.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @NotNull CurrencyCode currency,
        @Size(max = 280) String description,
        boolean allowFrozenAdminOverride
) {
}
