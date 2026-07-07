package com.bankcore.alert;

import com.bankcore.alert.domain.SuspiciousActivityAlert;
import com.bankcore.alert.repository.SuspiciousActivityAlertRepository;
import com.bankcore.alert.service.AlertService;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AlertSeverity;
import com.bankcore.common.enums.AlertStatus;
import com.bankcore.common.enums.AlertType;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    SuspiciousActivityAlertRepository alertRepository;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    AuditService auditService;
    @InjectMocks
    AlertService alertService;

    @Test
    void reviewMarksAlertReviewedAndAudits() {
        UUID alertId = UUID.randomUUID();
        SuspiciousActivityAlert alert = alert(alertId);
        AuthenticatedUser reviewer = employee();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(currentUserService.currentUser()).thenReturn(reviewer);

        SuspiciousActivityAlert reviewed = alertService.review(alertId, "ok");

        assertThat(reviewed.getStatus()).isEqualTo(AlertStatus.REVIEWED);
        assertThat(reviewed.getReviewedBy()).isEqualTo(reviewer.id());
        assertThat(reviewed.getReviewedAt()).isNotNull();
        verify(auditService).record(any(), any(), any(), any(), any());
    }

    @Test
    void dismissMarksAlertDismissed() {
        UUID alertId = UUID.randomUUID();
        SuspiciousActivityAlert alert = alert(alertId);
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(currentUserService.currentUser()).thenReturn(employee());

        SuspiciousActivityAlert dismissed = alertService.dismiss(alertId, null);

        assertThat(dismissed.getStatus()).isEqualTo(AlertStatus.DISMISSED);
    }

    private SuspiciousActivityAlert alert(UUID id) {
        return SuspiciousActivityAlert.builder()
                .id(id)
                .accountId(UUID.randomUUID())
                .alertType(AlertType.LARGE_TRANSFER)
                .severity(AlertSeverity.HIGH)
                .message("Large transfer")
                .status(AlertStatus.OPEN)
                .build();
    }

    private AuthenticatedUser employee() {
        return new AuthenticatedUser(UUID.randomUUID(), "employee@bankcore.local", "hash", UserRole.ROLE_BANK_EMPLOYEE, UserStatus.ACTIVE);
    }
}
