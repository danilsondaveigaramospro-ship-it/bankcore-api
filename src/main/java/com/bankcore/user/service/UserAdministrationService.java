package com.bankcore.user.service;

import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.common.exception.DuplicateResourceException;
import com.bankcore.common.exception.ResourceNotFoundException;
import com.bankcore.security.CurrentUserService;
import com.bankcore.user.domain.User;
import com.bankcore.user.dto.CreateEmployeeRequest;
import com.bankcore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAdministrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional
    public User createEmployee(CreateEmployeeRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_BANK_EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        auditService.record(currentUserService.currentUser().id(), "EMPLOYEE_CREATED", "User", user.getId().toString(), "{}");
        return user;
    }

    @Transactional
    public User disableUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.DISABLED);
        auditService.record(currentUserService.currentUser().id(), "USER_DISABLED", "User", userId.toString(), "{}");
        return user;
    }
}
