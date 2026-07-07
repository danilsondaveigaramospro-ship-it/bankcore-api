package com.bankcore.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        String action,
        String entityType,
        String entityId,
        String ipAddress,
        String userAgent,
        String metadata,
        Instant createdAt
) {
}
