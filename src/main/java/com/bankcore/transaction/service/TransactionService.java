package com.bankcore.transaction.service;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.dto.DepositRequest;
import com.bankcore.account.dto.WithdrawRequest;
import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.alert.service.SuspiciousActivityService;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.exception.AccountClosedException;
import com.bankcore.common.exception.AccountFrozenException;
import com.bankcore.common.exception.BusinessException;
import com.bankcore.common.exception.InsufficientFundsException;
import com.bankcore.common.exception.InvalidCurrencyException;
import com.bankcore.common.exception.ResourceNotFoundException;
import com.bankcore.common.exception.UnauthorizedOperationException;
import com.bankcore.common.util.MoneyUtils;
import com.bankcore.common.util.ReferenceGenerator;
import com.bankcore.security.AccountAccessPolicy;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final AccountAccessPolicy accountAccessPolicy;
    private final ReferenceGenerator referenceGenerator;
    private final AuditService auditService;
    private final SuspiciousActivityService suspiciousActivityService;
    private final FailedOperationRecorder failedOperationRecorder;

    @Transactional
    public BankTransaction deposit(UUID accountId, DepositRequest request) {
        AuthenticatedUser actor = currentUserService.currentUser();
        BigDecimal amount = MoneyUtils.requirePositive(request.amount());
        try {
            BankAccount account = accountRepository.findByIdForUpdate(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
            ensureCurrency(account, request.currency());
            ensureCanCredit(account, request.allowFrozenAdminOverride(), actor);
            account.setBalance(account.getBalance().add(amount));
            BankTransaction transaction = completedTransaction(
                    TransactionType.DEPOSIT,
                    null,
                    account.getId(),
                    amount,
                    account.getCurrency(),
                    descriptionOrDefault(request.description(), "Administrative deposit"),
                    actor.id(),
                    "DEP"
            );
            transactionRepository.save(transaction);
            auditService.record(actor.id(), "ACCOUNT_DEPOSIT", "BankAccount", accountId.toString(), "{\"transactionId\":\"" + transaction.getId() + "\"}");
            return transaction;
        } catch (BusinessException ex) {
            if (accountRepository.existsById(accountId)) {
                failedOperationRecorder.recordFailed(null, accountId, amount, request.currency(), request.description(), actor.id(), TransactionType.DEPOSIT, ex.getMessage());
            }
            throw ex;
        }
    }

    @Transactional
    public BankTransaction withdraw(UUID accountId, WithdrawRequest request) {
        AuthenticatedUser actor = currentUserService.currentUser();
        BigDecimal amount = MoneyUtils.requirePositive(request.amount());
        try {
            BankAccount account = accountRepository.findByIdForUpdate(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
            BigDecimal balanceBefore = account.getBalance();
            ensureCurrency(account, request.currency());
            ensureCanDebit(account);
            if (balanceBefore.compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }
            account.setBalance(balanceBefore.subtract(amount));
            BankTransaction transaction = completedTransaction(
                    TransactionType.WITHDRAWAL,
                    account.getId(),
                    null,
                    amount,
                    account.getCurrency(),
                    descriptionOrDefault(request.description(), "Administrative withdrawal"),
                    actor.id(),
                    "WDL"
            );
            transactionRepository.save(transaction);
            suspiciousActivityService.evaluateWithdrawal(account, transaction, balanceBefore);
            auditService.record(actor.id(), "ACCOUNT_WITHDRAWAL", "BankAccount", accountId.toString(), "{\"transactionId\":\"" + transaction.getId() + "\"}");
            return transaction;
        } catch (BusinessException ex) {
            if (accountRepository.existsById(accountId)) {
                failedOperationRecorder.recordFailed(accountId, null, amount, request.currency(), request.description(), actor.id(), TransactionType.WITHDRAWAL, ex.getMessage());
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<BankTransaction> listTransactions() {
        AuthenticatedUser user = currentUserService.currentUser();
        if (user.role() == UserRole.ROLE_CUSTOMER) {
            return accountRepository.findByCustomer_User_Id(user.id()).stream()
                    .flatMap(account -> transactionRepository.findByAccountId(account.getId()).stream())
                    .sorted(Comparator.comparing(BankTransaction::getCreatedAt).reversed())
                    .toList();
        }
        return transactionRepository.findAll().stream()
                .sorted(Comparator.comparing(BankTransaction::getCreatedAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public BankTransaction getTransaction(UUID transactionId) {
        BankTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        ensureCanAccessTransaction(transaction);
        return transaction;
    }

    @Transactional(readOnly = true)
    public List<BankTransaction> getTransactionsForAccount(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    private void ensureCanAccessTransaction(BankTransaction transaction) {
        AuthenticatedUser user = currentUserService.currentUser();
        if (user.role() != UserRole.ROLE_CUSTOMER) {
            return;
        }
        boolean canAccessSource = transaction.getSourceAccountId() != null
                && accountAccessPolicy.canAccessAccount(transaction.getSourceAccountId());
        boolean canAccessTarget = transaction.getTargetAccountId() != null
                && accountAccessPolicy.canAccessAccount(transaction.getTargetAccountId());
        if (!canAccessSource && !canAccessTarget) {
            throw new UnauthorizedOperationException("Customer cannot access this transaction");
        }
    }

    private void ensureCurrency(BankAccount account, com.bankcore.common.enums.CurrencyCode requestedCurrency) {
        if (account.getCurrency() != requestedCurrency) {
            throw new InvalidCurrencyException("Operation currency must match account currency");
        }
    }

    private void ensureCanCredit(BankAccount account, boolean allowFrozenAdminOverride, AuthenticatedUser actor) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot receive deposits");
        }
        if (account.getStatus() == AccountStatus.FROZEN && !(allowFrozenAdminOverride && actor.role() == UserRole.ROLE_ADMIN)) {
            throw new AccountFrozenException("Frozen account cannot receive deposits without admin override");
        }
    }

    private void ensureCanDebit(BankAccount account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot be debited");
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Frozen account cannot be debited");
        }
    }

    private BankTransaction completedTransaction(TransactionType type, UUID sourceAccountId, UUID targetAccountId,
                                                 BigDecimal amount, com.bankcore.common.enums.CurrencyCode currency,
                                                 String description, UUID actorUserId, String referencePrefix) {
        return BankTransaction.builder()
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .currency(currency)
                .description(description)
                .reference(referenceGenerator.generate(referencePrefix))
                .completedAt(Instant.now())
                .initiatedByUserId(actorUserId)
                .build();
    }

    private String descriptionOrDefault(String description, String fallback) {
        return description == null || description.isBlank() ? fallback : description;
    }
}
