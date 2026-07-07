package com.bankcore.alert.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "bankcore.suspicious")
public record SuspiciousActivityProperties(
        BigDecimal largeTransferThreshold,
        int manyTransfersThreshold,
        int manyTransfersWindowMinutes,
        int failedAttemptsThreshold,
        int failedAttemptsWindowMinutes,
        BigDecimal unusualWithdrawalRatio
) {
}
