package com.bankcore.auth.service;

import com.bankcore.audit.service.AuditService;
import com.bankcore.auth.dto.AuthenticatedUserResponse;
import com.bankcore.auth.dto.LoginRequest;
import com.bankcore.auth.dto.LoginResponse;
import com.bankcore.auth.dto.RefreshTokenRequest;
import com.bankcore.auth.dto.RegisterCustomerRequest;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.common.exception.DuplicateResourceException;
import com.bankcore.common.exception.UnauthorizedOperationException;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.security.CurrentUserService;
import com.bankcore.security.JwtService;
import com.bankcore.user.domain.User;
import com.bankcore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional
    public LoginResponse registerCustomer(RegisterCustomerRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        CustomerProfile customer = CustomerProfile.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .phoneNumber(request.phoneNumber())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .postalCode(request.postalCode())
                .city(request.city())
                .country(request.country())
                .kycStatus(KycStatus.PENDING)
                .build();
        customerRepository.save(customer);
        auditService.record(user.getId(), "CUSTOMER_REGISTERED", "CustomerProfile", customer.getId().toString(), "{}");
        return issueTokens(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedOperationException("Invalid credentials"));
        user.setLastLoginAt(Instant.now());
        auditService.record(user.getId(), "USER_LOGIN", "User", user.getId().toString(), "{}");
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        if (!jwtService.isTokenType(request.refreshToken(), "refresh")) {
            throw new UnauthorizedOperationException("Invalid refresh token");
        }
        String email = jwtService.subject(request.refreshToken());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedOperationException("Invalid refresh token"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedOperationException("User is not active");
        }
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me() {
        UUID userId = currentUserService.currentUser().id();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedOperationException("Authenticated user does not exist"));
        UUID customerId = customerRepository.findByUser_Id(userId).map(CustomerProfile::getId).orElse(null);
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                customerId,
                user.getLastLoginAt()
        );
    }

    private LoginResponse issueTokens(User user) {
        return new LoginResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.accessExpiresAt(),
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
