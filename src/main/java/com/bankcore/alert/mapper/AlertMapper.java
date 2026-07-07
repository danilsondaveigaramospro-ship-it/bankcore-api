package com.bankcore.alert.mapper;

import com.bankcore.alert.domain.SuspiciousActivityAlert;
import com.bankcore.alert.dto.AlertResponse;

public final class AlertMapper {

    private AlertMapper() {
    }

    public static AlertResponse toResponse(SuspiciousActivityAlert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAccountId(),
                alert.getTransactionId(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getReviewedAt(),
                alert.getReviewedBy()
        );
    }
}
