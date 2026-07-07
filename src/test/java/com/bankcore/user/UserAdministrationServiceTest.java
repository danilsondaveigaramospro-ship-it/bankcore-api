package com.bankcore.user;

import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.user.domain.User;
import com.bankcore.user.dto.CreateEmployeeRequest;
import com.bankcore.user.repository.UserRepository;
import com.bankcore.user.service.UserAdministrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdministrationServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    AuditService auditService;
    @InjectMocks
    UserAdministrationService userAdministrationService;

    @Test
    void adminCreatesEmployee() {
        when(userRepository.existsByEmailIgnoreCase("new.employee@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(currentUserService.currentUser()).thenReturn(admin());

        User employee = userAdministrationService.createEmployee(new CreateEmployeeRequest("new.employee@example.com", "Password123!"));

        assertThat(employee.getRole()).isEqualTo(UserRole.ROLE_BANK_EMPLOYEE);
        assertThat(employee.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void adminDisablesUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("user@example.com").status(UserStatus.ACTIVE).role(UserRole.ROLE_CUSTOMER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(currentUserService.currentUser()).thenReturn(admin());

        User disabled = userAdministrationService.disableUser(userId);

        assertThat(disabled.getStatus()).isEqualTo(UserStatus.DISABLED);
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), "admin@bankcore.local", "hash", UserRole.ROLE_ADMIN, UserStatus.ACTIVE);
    }
}
