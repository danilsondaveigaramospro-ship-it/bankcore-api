package com.bankcore.security;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("accountAccessPolicy")
@RequiredArgsConstructor
public class AccountAccessPolicy {

    private final BankAccountRepository accountRepository;

    public boolean canAccessAccount(UUID accountId) {
        AuthenticatedUser user = principal();
        if (user == null) {
            return false;
        }
        if (user.role() == UserRole.ROLE_ADMIN || user.role() == UserRole.ROLE_BANK_EMPLOYEE) {
            return true;
        }
        return accountRepository.existsByIdAndCustomer_User_Id(accountId, user.id());
    }

    public boolean canInitiateTransferFrom(UUID accountId) {
        return canAccessAccount(accountId);
    }

    public boolean isOwner(BankAccount account, UUID userId) {
        return account.getCustomer().getUser().getId().equals(userId);
    }

    private AuthenticatedUser principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }
}
