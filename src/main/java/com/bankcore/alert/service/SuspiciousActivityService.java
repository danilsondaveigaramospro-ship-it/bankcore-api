package com.bankcore.alert.service;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.alert.domain.SuspiciousActivityAlert;
import com.bankcore.alert.repository.SuspiciousActivityAlertRepository;
import com.bankcore.common.enums.AlertSeverity;
import com.bankcore.common.enums.AlertStatus;
import com.bankcore.common.enums.AlertType;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuspiciousActivityService {

    private final SuspiciousActivityProperties properties;
    private final SuspiciousActivityAlertRepository alertRepository;
    private final BankTransactionRepository transactionRepository;

    public void evaluateTransfer(BankAccount sourceAccount, BankTransaction outgoingTransaction) {
        BigDecimal amount = outgoingTransaction.getAmount();
        if (amount.compareTo(properties.largeTransferThreshold()) > 0) {
            createAlert(
                    sourceAccount.getId(),
                    outgoingTransaction.getId(),
                    AlertType.LARGE_TRANSFER,
                    AlertSeverity.HIGH,
                    "Transfer exceeds configured large-transfer threshold"
            );
        }

        Instant since = Instant.now().minusSeconds(properties.manyTransfersWindowMinutes() * 60L);
        long recentTransfers = transactionRepository.countBySourceAccountIdAndTypeAndStatusAndCreatedAtAfter(
                sourceAccount.getId(),
                TransactionType.TRANSFER_OUT,
                TransactionStatus.COMPLETED,
                since
        );
        if (recentTransfers > properties.manyTransfersThreshold()) {
            createAlert(
                    sourceAccount.getId(),
                    outgoingTransaction.getId(),
                    AlertType.MANY_TRANSFERS,
                    AlertSeverity.MEDIUM,
                    "More outgoing transfers than allowed by suspicious-activity threshold"
            );
        }
    }

    public void evaluateWithdrawal(BankAccount account, BankTransaction withdrawalTransaction, BigDecimal balanceBeforeWithdrawal) {
        BigDecimal threshold = balanceBeforeWithdrawal.multiply(properties.unusualWithdrawalRatio());
        if (withdrawalTransaction.getAmount().compareTo(threshold) > 0) {
            createAlert(
                    account.getId(),
                    withdrawalTransaction.getId(),
                    AlertType.UNUSUAL_WITHDRAWAL,
                    AlertSeverity.MEDIUM,
                    "Withdrawal exceeds configured ratio of available balance"
            );
        }
    }

    public void evaluateFailedAttempts(UUID accountId) {
        Instant since = Instant.now().minusSeconds(properties.failedAttemptsWindowMinutes() * 60L);
        long failures = transactionRepository.countFailedForAccountSince(accountId, since);
        if (failures > properties.failedAttemptsThreshold()) {
            createAlert(
                    accountId,
                    null,
                    AlertType.FAILED_ATTEMPTS,
                    AlertSeverity.MEDIUM,
                    "Too many failed or rejected operations on account"
            );
        }
    }

    private void createAlert(UUID accountId, UUID transactionId, AlertType type, AlertSeverity severity, String message) {
        SuspiciousActivityAlert alert = SuspiciousActivityAlert.builder()
                .accountId(accountId)
                .transactionId(transactionId)
                .alertType(type)
                .severity(severity)
                .message(message)
                .status(AlertStatus.OPEN)
                .build();
        alertRepository.save(alert);
    }
}
