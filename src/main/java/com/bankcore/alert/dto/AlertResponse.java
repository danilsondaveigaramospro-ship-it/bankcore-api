package com.bankcore.alert.dto;

import com.bankcore.common.enums.AlertSeverity;
import com.bankcore.common.enums.AlertStatus;
import com.bankcore.common.enums.AlertType;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID accountId,
        UUID transactionId,
        AlertType alertType,
        AlertSeverity severity,
        String message,
        AlertStatus status,
        Instant createdAt,
        Instant reviewedAt,
        UUID reviewedBy
) {
}
