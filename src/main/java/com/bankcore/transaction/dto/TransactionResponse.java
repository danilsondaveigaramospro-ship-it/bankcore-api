package com.bankcore.transaction.dto;

import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        TransactionStatus status,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        CurrencyCode currency,
        String description,
        String reference,
        String failureReason,
        Instant createdAt,
        Instant completedAt,
        UUID initiatedByUserId
) {
}
