package com.bankcore.audit.mapper;

import com.bankcore.audit.domain.AuditLog;
import com.bankcore.audit.dto.AuditLogResponse;

public final class AuditLogMapper {

    private AuditLogMapper() {
    }

    public static AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getMetadata(),
                log.getCreatedAt()
        );
    }
}
