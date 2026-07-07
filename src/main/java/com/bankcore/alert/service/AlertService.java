package com.bankcore.alert.service;

import com.bankcore.alert.domain.SuspiciousActivityAlert;
import com.bankcore.alert.repository.SuspiciousActivityAlertRepository;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AlertStatus;
import com.bankcore.common.exception.ResourceNotFoundException;
import com.bankcore.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final SuspiciousActivityAlertRepository alertRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public List<SuspiciousActivityAlert> findAll() {
        return alertRepository.findAll();
    }

    public SuspiciousActivityAlert getById(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
    }

    @Transactional
    public SuspiciousActivityAlert review(UUID id, String note) {
        SuspiciousActivityAlert alert = getById(id);
        UUID actorId = currentUserService.currentUser().id();
        alert.setStatus(AlertStatus.REVIEWED);
        alert.setReviewedAt(Instant.now());
        alert.setReviewedBy(actorId);
        auditService.record(actorId, "ALERT_REVIEWED", "SuspiciousActivityAlert", id.toString(), jsonNote(note));
        return alert;
    }

    @Transactional
    public SuspiciousActivityAlert dismiss(UUID id, String note) {
        SuspiciousActivityAlert alert = getById(id);
        UUID actorId = currentUserService.currentUser().id();
        alert.setStatus(AlertStatus.DISMISSED);
        alert.setReviewedAt(Instant.now());
        alert.setReviewedBy(actorId);
        auditService.record(actorId, "ALERT_DISMISSED", "SuspiciousActivityAlert", id.toString(), jsonNote(note));
        return alert;
    }

    private String jsonNote(String note) {
        if (note == null || note.isBlank()) {
            return "{}";
        }
        return "{\"note\":\"" + note.replace("\"", "'") + "\"}";
    }
}
