package com.bankcore.alert.repository;

import com.bankcore.alert.domain.SuspiciousActivityAlert;
import com.bankcore.common.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SuspiciousActivityAlertRepository extends JpaRepository<SuspiciousActivityAlert, UUID> {
    List<SuspiciousActivityAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    long countByStatus(AlertStatus status);
}
