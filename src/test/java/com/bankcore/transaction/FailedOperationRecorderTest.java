package com.bankcore.transaction;

import com.bankcore.alert.service.SuspiciousActivityService;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.common.util.ReferenceGenerator;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.repository.BankTransactionRepository;
import com.bankcore.transaction.service.FailedOperationRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailedOperationRecorderTest {

    @Mock
    BankTransactionRepository transactionRepository;
    @Mock
    ReferenceGenerator referenceGenerator;
    @Mock
    SuspiciousActivityService suspiciousActivityService;
    @InjectMocks
    FailedOperationRecorder recorder;

    @Test
    void recordsFailedTransactionAndEvaluatesFailedAttempts() {
        UUID accountId = UUID.randomUUID();
        when(referenceGenerator.generate("FAIL")).thenReturn("FAIL-1");

        recorder.recordFailed(accountId, null, new BigDecimal("10.00"), CurrencyCode.CHF, "failed", UUID.randomUUID(), TransactionType.WITHDRAWAL, "No funds");

        ArgumentCaptor<BankTransaction> captor = ArgumentCaptor.forClass(BankTransaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus().name()).isEqualTo("FAILED");
        assertThat(captor.getValue().getFailureReason()).isEqualTo("No funds");
        verify(suspiciousActivityService).evaluateFailedAttempts(accountId);
    }

    @Test
    void skipsInvalidAmount() {
        recorder.recordFailed(UUID.randomUUID(), null, BigDecimal.ZERO, CurrencyCode.CHF, "failed", UUID.randomUUID(), TransactionType.WITHDRAWAL, "invalid");

        verify(transactionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
