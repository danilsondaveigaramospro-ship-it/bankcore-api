package com.bankcore.dashboard.service;

import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.alert.repository.SuspiciousActivityAlertRepository;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AlertStatus;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.dashboard.dto.DashboardResponse;
import com.bankcore.transaction.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerProfileRepository customerRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final SuspiciousActivityAlertRepository alertRepository;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = startOfDay.plusSeconds(24 * 60 * 60);
        Map<CurrencyCode, BigDecimal> balances = new EnumMap<>(CurrencyCode.class);
        for (CurrencyCode currency : CurrencyCode.values()) {
            balances.put(currency, accountRepository.totalBalanceForCurrency(currency.name()));
        }
        return new DashboardResponse(
                customerRepository.count(),
                accountRepository.count(),
                balances,
                transactionRepository.countByCreatedAtBetween(startOfDay, endOfDay),
                transactionRepository.countByCreatedAtBetweenAndStatus(startOfDay, endOfDay, TransactionStatus.FAILED),
                alertRepository.countByStatus(AlertStatus.OPEN),
                accountRepository.countByStatus(AccountStatus.FROZEN)
        );
    }
}
