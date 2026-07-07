package com.bankcore.dashboard.dto;

import com.bankcore.common.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardResponse(
        long totalCustomers,
        long totalAccounts,
        Map<CurrencyCode, BigDecimal> totalBalanceByCurrency,
        long transactionsToday,
        long failedTransactionsToday,
        long openSuspiciousAlerts,
        long frozenAccounts
) {
}
