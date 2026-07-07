package com.bankcore.security;

import com.bankcore.common.enums.UserRole;
import com.bankcore.customer.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("customerAccessPolicy")
@RequiredArgsConstructor
public class CustomerAccessPolicy {

    private final CustomerProfileRepository customerRepository;

    public boolean canAccessCustomer(UUID customerId) {
        AuthenticatedUser user = principal();
        if (user == null) {
            return false;
        }
        if (user.role() == UserRole.ROLE_ADMIN || user.role() == UserRole.ROLE_BANK_EMPLOYEE) {
            return true;
        }
        return customerRepository.existsByIdAndUser_Id(customerId, user.id());
    }

    private AuthenticatedUser principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }
}
