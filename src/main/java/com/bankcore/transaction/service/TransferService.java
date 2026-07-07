package com.bankcore.transaction.service;

import com.bankcore.account.domain.BankAccount;
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
import com.bankcore.common.exception.DailyLimitExceededException;
import com.bankcore.common.exception.InsufficientFundsException;
import com.bankcore.common.exception.InvalidCurrencyException;
import com.bankcore.common.exception.ResourceNotFoundException;
import com.bankcore.common.exception.UnauthorizedOperationException;
import com.bankcore.common.exception.ValidationException;
import com.bankcore.common.util.MoneyUtils;
import com.bankcore.common.util.ReferenceGenerator;
import com.bankcore.security.AccountAccessPolicy;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.dto.TransferRequest;
import com.bankcore.transaction.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final AccountAccessPolicy accountAccessPolicy;
    private final ReferenceGenerator referenceGenerator;
    private final AuditService auditService;
    private final SuspiciousActivityService suspiciousActivityService;
    private final FailedOperationRecorder failedOperationRecorder;

    @Transactional
    public TransferResult transfer(TransferRequest request) {
        AuthenticatedUser actor = currentUserService.currentUser();
        BigDecimal amount = MoneyUtils.requirePositive(request.amount());
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new ValidationException("Source and target accounts must be different");
        }
        try {
            Map<UUID, BankAccount> accounts = lockTransferAccounts(request.sourceAccountId(), request.targetAccountId());
            BankAccount source = accountOrThrow(accounts, request.sourceAccountId());
            BankAccount target = accountOrThrow(accounts, request.targetAccountId());

            if (actor.role() == UserRole.ROLE_CUSTOMER && !accountAccessPolicy.isOwner(source, actor.id())) {
                throw new UnauthorizedOperationException("Customer can transfer only from own account");
            }
            validateTransfer(source, target, request, amount);

            source.setBalance(source.getBalance().subtract(amount));
            target.setBalance(target.getBalance().add(amount));

            String referenceBase = referenceGenerator.generate("TRF");
            String description = request.description() == null || request.description().isBlank()
                    ? "Internal transfer"
                    : request.description();
            BankTransaction outgoing = completedTransferTransaction(
                    TransactionType.TRANSFER_OUT,
                    source.getId(),
                    target.getId(),
                    amount,
                    source.getCurrency(),
                    description,
                    referenceBase + "-OUT",
                    actor.id()
            );
            BankTransaction incoming = completedTransferTransaction(
                    TransactionType.TRANSFER_IN,
                    source.getId(),
                    target.getId(),
                    amount,
                    target.getCurrency(),
                    description,
                    referenceBase + "-IN",
                    actor.id()
            );
            transactionRepository.save(outgoing);
            transactionRepository.save(incoming);
            suspiciousActivityService.evaluateTransfer(source, outgoing);
            auditService.record(actor.id(), "INTERNAL_TRANSFER", "BankTransaction", outgoing.getId().toString(), "{\"incomingTransactionId\":\"" + incoming.getId() + "\"}");
            return new TransferResult(outgoing, incoming);
        } catch (BusinessException ex) {
            UUID failedSource = accountRepository.existsById(request.sourceAccountId()) ? request.sourceAccountId() : null;
            UUID failedTarget = accountRepository.existsById(request.targetAccountId()) ? request.targetAccountId() : null;
            if (failedSource != null || failedTarget != null) {
                failedOperationRecorder.recordFailed(
                        failedSource,
                        failedTarget,
                        amount,
                        request.currency(),
                        request.description(),
                        actor.id(),
                        TransactionType.TRANSFER_OUT,
                        ex.getMessage()
                );
            }
            throw ex;
        }
    }

    private Map<UUID, BankAccount> lockTransferAccounts(UUID sourceAccountId, UUID targetAccountId) {
        List<BankAccount> accounts = accountRepository.findAllByIdInForUpdate(List.of(sourceAccountId, targetAccountId));
        return accounts.stream().collect(Collectors.toMap(BankAccount::getId, Function.identity()));
    }

    private BankAccount accountOrThrow(Map<UUID, BankAccount> accounts, UUID accountId) {
        BankAccount account = accounts.get(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }
        return account;
    }

    private void validateTransfer(BankAccount source, BankAccount target, TransferRequest request, BigDecimal amount) {
        if (source.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Source account is closed");
        }
        if (source.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Source account is frozen");
        }
        if (target.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Target account is closed");
        }
        if (source.getCurrency() != target.getCurrency() || source.getCurrency() != request.currency()) {
            throw new InvalidCurrencyException("Transfers require matching source, target, and request currency");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal alreadyTransferred = transactionRepository.sumCompletedOutgoingTransfersSince(source.getId(), startOfDay);
        if (alreadyTransferred.add(amount).compareTo(source.getDailyTransferLimit()) > 0) {
            throw new DailyLimitExceededException("Daily transfer limit exceeded");
        }
    }

    private BankTransaction completedTransferTransaction(TransactionType type, UUID sourceAccountId, UUID targetAccountId,
                                                         BigDecimal amount, com.bankcore.common.enums.CurrencyCode currency,
                                                         String description, String reference, UUID actorUserId) {
        return BankTransaction.builder()
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .currency(currency)
                .description(description)
                .reference(reference)
                .completedAt(Instant.now())
                .initiatedByUserId(actorUserId)
                .build();
    }

    public record TransferResult(BankTransaction outgoing, BankTransaction incoming) {
    }
}
