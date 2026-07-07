package com.bankcore.alert.controller;

import com.bankcore.alert.dto.AlertResponse;
import com.bankcore.alert.dto.ReviewAlertRequest;
import com.bankcore.alert.mapper.AlertMapper;
import com.bankcore.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Suspicious Activity Alerts")
@PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "List suspicious activity alerts")
    public List<AlertResponse> listAlerts() {
        return alertService.findAll().stream().map(AlertMapper::toResponse).toList();
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get alert details")
    public AlertResponse getAlert(@PathVariable UUID alertId) {
        return AlertMapper.toResponse(alertService.getById(alertId));
    }

    @PatchMapping("/{alertId}/review")
    @Operation(summary = "Mark alert as reviewed")
    public AlertResponse review(@PathVariable UUID alertId, @Valid @RequestBody ReviewAlertRequest request) {
        return AlertMapper.toResponse(alertService.review(alertId, request.note()));
    }

    @PatchMapping("/{alertId}/dismiss")
    @Operation(summary = "Dismiss an alert")
    public AlertResponse dismiss(@PathVariable UUID alertId, @Valid @RequestBody ReviewAlertRequest request) {
        return AlertMapper.toResponse(alertService.dismiss(alertId, request.note()));
    }
}
