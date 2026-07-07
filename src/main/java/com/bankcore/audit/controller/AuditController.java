package com.bankcore.audit.controller;

import com.bankcore.audit.dto.AuditLogResponse;
import com.bankcore.audit.mapper.AuditLogMapper;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs")
@PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "List audit logs")
    public List<AuditLogResponse> listAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return auditService.findAll(PageRequest.of(page, Math.min(size, 200))).stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }

    @GetMapping("/{auditLogId}")
    @Operation(summary = "Get audit log details")
    public AuditLogResponse getAuditLog(@PathVariable UUID auditLogId) {
        return auditService.findById(auditLogId)
                .map(AuditLogMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found"));
    }
}
