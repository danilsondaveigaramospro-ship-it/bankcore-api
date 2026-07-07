package com.bankcore.audit.service;

import com.bankcore.audit.domain.AuditLog;
import com.bankcore.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(UUID actorUserId, String action, String entityType, String entityId, String metadata) {
        RequestInfo requestInfo = currentRequestInfo();
        AuditLog log = AuditLog.builder()
                .actorUserId(actorUserId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(requestInfo.ipAddress())
                .userAgent(requestInfo.userAgent())
                .metadata(metadata)
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    public Optional<AuditLog> findById(UUID id) {
        return auditLogRepository.findById(id);
    }

    private RequestInfo currentRequestInfo() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            String ip = forwardedFor == null || forwardedFor.isBlank()
                    ? request.getRemoteAddr()
                    : forwardedFor.split(",")[0].trim();
            return new RequestInfo(ip, request.getHeader("User-Agent"));
        }
        return new RequestInfo(null, null);
    }

    private record RequestInfo(String ipAddress, String userAgent) {
    }
}
