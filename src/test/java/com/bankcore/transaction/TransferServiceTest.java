package com.bankcore.transaction;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.alert.service.SuspiciousActivityService;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.common.exception.AccountClosedException;
import com.bankcore.common.exception.AccountFrozenException;
import com.bankcore.common.exception.DailyLimitExceededException;
import com.bankcore.common.util.ReferenceGenerator;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.security.AccountAccessPolicy;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.transaction.domain.BankTransaction;
import com.bankcore.transaction.dto.TransferRequest;
import com.bankcore.transaction.repository.BankTransactionRepository;
import com.bankcore.transaction.service.FailedOperationRecorder;
import com.bankcore.transaction.service.TransferService;
import com.bankcore.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

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
    TransferService transferService;

    @Test
    void validTransferUpdatesBothAccountsAtomically() {
        UUID customerUserId = UUID.randomUUID();
        BankAccount source = account(UUID.randomUUID(), customerUserId, new BigDecimal("1000.00"), AccountStatus.ACTIVE);
        BankAccount target = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("200.00"), AccountStatus.ACTIVE);
        when(currentUserService.currentUser()).thenReturn(customer(customerUserId));
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(source, target));
        when(accountAccessPolicy.isOwner(source, customerUserId)).thenReturn(true);
        when(transactionRepository.sumCompletedOutgoingTransfersSince(any(), any())).thenReturn(BigDecimal.ZERO);
        when(referenceGenerator.generate("TRF")).thenReturn("TRF-1");
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> {
            BankTransaction transaction = invocation.getArgument(0);
            transaction.setId(UUID.randomUUID());
            return transaction;
        });

        TransferService.TransferResult result = transferService.transfer(new TransferRequest(
                source.getId(),
                target.getId(),
                new BigDecimal("250.00"),
                CurrencyCode.CHF,
                "rent"
        ));

        assertThat(source.getBalance()).isEqualByComparingTo("750.00");
        assertThat(target.getBalance()).isEqualByComparingTo("450.00");
        assertThat(result.outgoing().getType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(result.incoming().getType()).isEqualTo(TransactionType.TRANSFER_IN);
        verify(suspiciousActivityService).evaluateTransfer(source, result.outgoing());
    }

    @Test
    void transferToClosedAccountIsRejected() {
        BankAccount source = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000.00"), AccountStatus.ACTIVE);
        BankAccount target = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("200.00"), AccountStatus.CLOSED);
        when(currentUserService.currentUser()).thenReturn(employee());
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(source, target));

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(
                source.getId(), target.getId(), new BigDecimal("50.00"), CurrencyCode.CHF, "test"
        ))).isInstanceOf(AccountClosedException.class);
    }

    @Test
    void transferFromFrozenAccountIsRejected() {
        BankAccount source = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000.00"), AccountStatus.FROZEN);
        BankAccount target = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("200.00"), AccountStatus.ACTIVE);
        when(currentUserService.currentUser()).thenReturn(employee());
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(source, target));

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(
                source.getId(), target.getId(), new BigDecimal("50.00"), CurrencyCode.CHF, "test"
        ))).isInstanceOf(AccountFrozenException.class);
    }

    @Test
    void transferOverDailyLimitIsRejected() {
        BankAccount source = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000.00"), AccountStatus.ACTIVE);
        source.setDailyTransferLimit(new BigDecimal("500.00"));
        BankAccount target = account(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("200.00"), AccountStatus.ACTIVE);
        when(currentUserService.currentUser()).thenReturn(employee());
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(source, target));
        when(transactionRepository.sumCompletedOutgoingTransfersSince(any(), any())).thenReturn(new BigDecimal("400.00"));

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(
                source.getId(), target.getId(), new BigDecimal("150.00"), CurrencyCode.CHF, "test"
        ))).isInstanceOf(DailyLimitExceededException.class);
    }

    private BankAccount account(UUID accountId, UUID userId, BigDecimal balance, AccountStatus status) {
        User user = User.builder().id(userId).email(userId + "@example.com").build();
        CustomerProfile customer = CustomerProfile.builder().id(UUID.randomUUID()).user(user).build();
        return BankAccount.builder()
                .id(accountId)
                .customer(customer)
                .iban("CH9300762011623852957")
                .accountNumber(accountId.toString().substring(0, 8))
                .currency(CurrencyCode.CHF)
                .balance(balance)
                .status(status)
                .accountType(AccountType.CHECKING)
                .dailyTransferLimit(new BigDecimal("10000.00"))
                .build();
    }

    private AuthenticatedUser customer(UUID userId) {
        return new AuthenticatedUser(userId, "alice@bankcore.local", "hash", UserRole.ROLE_CUSTOMER, UserStatus.ACTIVE);
    }

    private AuthenticatedUser employee() {
        return new AuthenticatedUser(UUID.randomUUID(), "employee@bankcore.local", "hash", UserRole.ROLE_BANK_EMPLOYEE, UserStatus.ACTIVE);
    }
}
