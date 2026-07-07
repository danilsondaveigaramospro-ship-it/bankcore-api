package com.bankcore.dashboard;

import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.alert.repository.SuspiciousActivityAlertRepository;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AlertStatus;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.dashboard.dto.DashboardResponse;
import com.bankcore.dashboard.service.DashboardService;
import com.bankcore.transaction.repository.BankTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    CustomerProfileRepository customerRepository;
    @Mock
    BankAccountRepository accountRepository;
    @Mock
    BankTransactionRepository transactionRepository;
    @Mock
    SuspiciousActivityAlertRepository alertRepository;
    @InjectMocks
    DashboardService dashboardService;

    @Test
    void dashboardAggregatesCoreMetrics() {
        when(customerRepository.count()).thenReturn(2L);
        when(accountRepository.count()).thenReturn(3L);
        when(accountRepository.totalBalanceForCurrency("CHF")).thenReturn(new BigDecimal("33000.00"));
        when(accountRepository.totalBalanceForCurrency("EUR")).thenReturn(new BigDecimal("5000.00"));
        when(accountRepository.totalBalanceForCurrency("USD")).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countByCreatedAtBetween(any(), any())).thenReturn(7L);
        when(transactionRepository.countByCreatedAtBetweenAndStatus(any(), any(), org.mockito.ArgumentMatchers.eq(TransactionStatus.FAILED))).thenReturn(1L);
        when(alertRepository.countByStatus(AlertStatus.OPEN)).thenReturn(4L);
        when(accountRepository.countByStatus(AccountStatus.FROZEN)).thenReturn(1L);

        DashboardResponse response = dashboardService.dashboard();

        assertThat(response.totalCustomers()).isEqualTo(2L);
        assertThat(response.totalAccounts()).isEqualTo(3L);
        assertThat(response.totalBalanceByCurrency().get(CurrencyCode.CHF)).isEqualByComparingTo("33000.00");
        assertThat(response.openSuspiciousAlerts()).isEqualTo(4L);
    }
}
