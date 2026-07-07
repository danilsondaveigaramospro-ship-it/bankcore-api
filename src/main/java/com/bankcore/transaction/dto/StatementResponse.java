package com.bankcore.transaction.dto;

import com.bankcore.account.dto.AccountResponse;

import java.time.Instant;
import java.util.List;

public record StatementResponse(
        AccountResponse account,
        Instant generatedAt,
        List<TransactionResponse> transactions
) {
}
