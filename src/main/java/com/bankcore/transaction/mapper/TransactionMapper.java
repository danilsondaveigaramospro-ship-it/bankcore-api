package com.bankcore.transaction.mapper;

import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.dto.TransactionResponse;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(BankTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getSourceAccountId(),
                transaction.getTargetAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getReference(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt(),
                transaction.getInitiatedByUserId()
        );
    }
}
