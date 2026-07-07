package com.bankcore.transaction;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.dto.DepositRequest;
import com.bankcore.account.dto.WithdrawRequest;
import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.alert.service.SuspiciousActivityService;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.common.exception.InsufficientFundsException;
import com.bankcore.common.util.ReferenceGenerator;
import com.bankcore.security.AccountAccessPolicy;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.repository.BankTransactionRepository;
import com.bankcore.transaction.service.FailedOperationRecorder;
import com.bankcore.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    BankAccountRepository accountRepository;
    @Mock
    BankTransactionRepository transactionRepository;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    AccountAccessPolicy accountAccessPolicy;
    @Mock
    ReferenceGenerator referenceGenerator;
    @Mock
    AuditService auditService;
    @Mock
    SuspiciousActivityService suspiciousActivityService;
    @Mock
    FailedOperationRecorder failedOperationRecorder;
    @InjectMocks
    TransactionService transactionService;

    @Test
    void validDepositIncreasesBalanceAndCreatesTransaction() {
        UUID accountId = UUID.randomUUID();
        BankAccount account = account(accountId, new BigDecimal("100.00"), AccountStatus.ACTIVE);
        when(currentUserService.currentUser()).thenReturn(admin());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(referenceGenerator.generate("DEP")).thenReturn("DEP-1");
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankTransaction transaction = transactionService.deposit(
                accountId,
                new DepositRequest(new BigDecimal("50.00"), CurrencyCode.CHF, "cash", false)
        );

        assertThat(account.getBalance()).isEqualByComparingTo("150.00");
        assertThat(transaction.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getStatus().name()).isEqualTo("COMPLETED");
    }

    @Test
    void validWithdrawalDecreasesBalanceAndCreatesTransaction() {
        UUID accountId = UUID.randomUUID();
        BankAccount account = account(accountId, new BigDecimal("100.00"), AccountStatus.ACTIVE);
        when(currentUserService.currentUser()).thenReturn(admin());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(referenceGenerator.generate("WDL")).thenReturn("WDL-1");
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankTransaction transaction = transactionService.withdraw(
                accountId,
                new WithdrawRequest(new BigDecimal("40.00"), CurrencyCode.CHF, "cash")
        );

        assertThat(account.getBalance()).isEqualByComparingTo("60.00");
        assertThat(transaction.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        verify(suspiciousActivityService).evaluateWithdrawal(eq(account), eq(transaction), eq(new BigDecimal("100.00")));
    }

    @Test
    void withdrawalWithInsufficientFundsIsRejected() {
        UUID accountId = UUID.randomUUID();
        BankAccount account = account(accountId, new BigDecimal("30.00"), AccountStatus.ACTIVE);
        when(currentUserService.currentUser()).thenReturn(admin());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.existsById(accountId)).thenReturn(true);

        assertThatThrownBy(() -> transactionService.withdraw(
                accountId,
                new WithdrawRequest(new BigDecimal("40.00"), CurrencyCode.CHF, "cash")
        )).isInstanceOf(InsufficientFundsException.class);

        assertThat(account.getBalance()).isEqualByComparingTo("30.00");
        verify(failedOperationRecorder).recordFailed(eq(accountId), eq(null), any(), eq(CurrencyCode.CHF), eq("cash"), any(), eq(TransactionType.WITHDRAWAL), eq("Insufficient balance"));
    }

    private BankAccount account(UUID id, BigDecimal balance, AccountStatus status) {
        return BankAccount.builder()
                .id(id)
                .iban("CH9300762011623852957")
                .accountNumber("1000000001")
                .currency(CurrencyCode.CHF)
                .balance(balance)
                .status(status)
                .accountType(AccountType.CHECKING)
                .dailyTransferLimit(new BigDecimal("10000.00"))
                .build();
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), "admin@bankcore.local", "hash", UserRole.ROLE_ADMIN, UserStatus.ACTIVE);
    }
}
