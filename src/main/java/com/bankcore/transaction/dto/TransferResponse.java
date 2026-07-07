package com.bankcore.transaction.dto;

public record TransferResponse(
        TransactionResponse outgoing,
        TransactionResponse incoming
) {
}
