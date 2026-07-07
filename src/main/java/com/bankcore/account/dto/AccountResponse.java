package com.bankcore.account.dto;

import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        String iban,
        String accountNumber,
        CurrencyCode currency,
        BigDecimal balance,
        AccountStatus status,
        AccountType accountType,
        BigDecimal dailyTransferLimit,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
}
