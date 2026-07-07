package com.bankcore.security;

import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAccessPolicyTest {

    @Mock
    BankAccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerCannotAccessAnotherCustomerAccount() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        authenticate(new AuthenticatedUser(userId, "alice@bankcore.local", "hash", UserRole.ROLE_CUSTOMER, UserStatus.ACTIVE));
        when(accountRepository.existsByIdAndCustomer_User_Id(accountId, userId)).thenReturn(false);

        AccountAccessPolicy policy = new AccountAccessPolicy(accountRepository);

        assertThat(policy.canAccessAccount(accountId)).isFalse();
    }

    @Test
    void employeeCanAccessAnyAccount() {
        authenticate(new AuthenticatedUser(UUID.randomUUID(), "employee@bankcore.local", "hash", UserRole.ROLE_BANK_EMPLOYEE, UserStatus.ACTIVE));

        AccountAccessPolicy policy = new AccountAccessPolicy(accountRepository);

        assertThat(policy.canAccessAccount(UUID.randomUUID())).isTrue();
    }

    private void authenticate(AuthenticatedUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }
}
