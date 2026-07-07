package com.bankcore.account;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.dto.CreateAccountRequest;
import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.account.service.AccountService;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.common.exception.ValidationException;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    BankAccountRepository accountRepository;
    @Mock
    CustomerProfileRepository customerRepository;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    AuditService auditService;
    @InjectMocks
    AccountService accountService;

    @Test
    void createsAccountOnlyForVerifiedCustomer() {
        UUID customerId = UUID.randomUUID();
        CustomerProfile customer = CustomerProfile.builder()
                .id(customerId)
                .firstName("Alice")
                .lastName("Martin")
                .dateOfBirth(LocalDate.of(1991, 4, 12))
                .kycStatus(KycStatus.VERIFIED)
                .build();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.existsByIban(any())).thenReturn(false);
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0);
            account.setId(UUID.randomUUID());
            return account;
        });
        when(currentUserService.currentUser()).thenReturn(admin());

        accountService.createAccount(new CreateAccountRequest(
                customerId,
                CurrencyCode.CHF,
                AccountType.CHECKING,
                new BigDecimal("5000.00")
        ));

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(accountRepository).save(captor.capture());
        BankAccount saved = captor.getValue();
        assertThat(saved.getBalance()).isEqualByComparingTo("0.00");
        assertThat(saved.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(saved.getIban()).startsWith("CH");
        assertThat(saved.getCurrency()).isEqualTo(CurrencyCode.CHF);
    }

    @Test
    void freezeAndUnfreezeAccountAuditReason() {
        UUID accountId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(accountId)
                .balance(BigDecimal.ZERO)
                .currency(CurrencyCode.CHF)
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.CHECKING)
                .dailyTransferLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(currentUserService.currentUser()).thenReturn(admin());

        accountService.freeze(accountId, "fraud review");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.FROZEN);

        accountService.unfreeze(accountId, "cleared");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void closeRequiresZeroBalance() {
        UUID accountId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(accountId)
                .balance(new BigDecimal("1.00"))
                .currency(CurrencyCode.CHF)
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.CHECKING)
                .dailyTransferLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.close(accountId)).isInstanceOf(ValidationException.class);
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), "admin@bankcore.local", "hash", UserRole.ROLE_ADMIN, UserStatus.ACTIVE);
    }
}
