package com.bankcore.alert;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.alert.domain.SuspiciousActivityAlert;
import com.bankcore.alert.repository.SuspiciousActivityAlertRepository;
import com.bankcore.alert.service.SuspiciousActivityProperties;
import com.bankcore.alert.service.SuspiciousActivityService;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.AlertType;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.repository.BankTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspiciousActivityServiceTest {

    @Mock
    SuspiciousActivityAlertRepository alertRepository;
    @Mock
    BankTransactionRepository transactionRepository;

    @Test
    void createsAlertForLargeTransfer() {
        SuspiciousActivityService service = new SuspiciousActivityService(
                new SuspiciousActivityProperties(new BigDecimal("10000.00"), 5, 10, 3, 15, new BigDecimal("0.50")),
                alertRepository,
                transactionRepository
        );
        BankAccount account = BankAccount.builder()
                .id(UUID.randomUUID())
                .currency(CurrencyCode.CHF)
                .balance(new BigDecimal("50000.00"))
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.CHECKING)
                .build();
        BankTransaction transaction = BankTransaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(account.getId())
                .amount(new BigDecimal("15000.00"))
                .currency(CurrencyCode.CHF)
                .build();
        when(transactionRepository.countBySourceAccountIdAndTypeAndStatusAndCreatedAtAfter(any(), any(), any(), any()))
                .thenReturn(1L);

        service.evaluateTransfer(account, transaction);

        ArgumentCaptor<SuspiciousActivityAlert> captor = ArgumentCaptor.forClass(SuspiciousActivityAlert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getAlertType()).isEqualTo(AlertType.LARGE_TRANSFER);
        assertThat(captor.getValue().getStatus().name()).isEqualTo("OPEN");
    }
}
