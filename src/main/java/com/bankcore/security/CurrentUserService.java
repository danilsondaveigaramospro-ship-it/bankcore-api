package com.bankcore.security;

import com.bankcore.common.exception.UnauthorizedOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedOperationException("Authenticated user is required");
        }
        return user;
    }

    public boolean isAdminOrEmployee() {
        AuthenticatedUser user = currentUser();
        return user.role().name().equals("ROLE_ADMIN") || user.role().name().equals("ROLE_BANK_EMPLOYEE");
    }

    public boolean isAdmin() {
        return currentUser().role().name().equals("ROLE_ADMIN");
    }
}
