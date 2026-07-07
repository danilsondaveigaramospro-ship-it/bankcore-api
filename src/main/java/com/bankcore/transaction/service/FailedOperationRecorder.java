package com.bankcore.transaction.service;

import com.bankcore.alert.service.SuspiciousActivityService;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.common.util.ReferenceGenerator;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FailedOperationRecorder {

    private final BankTransactionRepository transactionRepository;
    private final ReferenceGenerator referenceGenerator;
    private final SuspiciousActivityService suspiciousActivityService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailed(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, CurrencyCode currency,
                             String description, UUID actorUserId, TransactionType type, String reason) {
        if (amount == null || amount.signum() <= 0 || currency == null) {
            return;
        }
        BankTransaction transaction = BankTransaction.builder()
                .type(type)
                .status(TransactionStatus.FAILED)
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .currency(currency)
                .description(description == null || description.isBlank() ? "Failed banking operation" : description)
                .reference(referenceGenerator.generate("FAIL"))
                .failureReason(reason)
                .initiatedByUserId(actorUserId)
                .build();
        transactionRepository.save(transaction);
        UUID accountId = sourceAccountId == null ? targetAccountId : sourceAccountId;
        if (accountId != null) {
            suspiciousActivityService.evaluateFailedAttempts(accountId);
        }
    }
}
