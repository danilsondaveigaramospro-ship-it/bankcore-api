package com.bankcore.auth;

import com.bankcore.audit.service.AuditService;
import com.bankcore.auth.dto.LoginRequest;
import com.bankcore.auth.dto.LoginResponse;
import com.bankcore.auth.dto.RefreshTokenRequest;
import com.bankcore.auth.dto.RegisterCustomerRequest;
import com.bankcore.auth.service.AuthService;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.security.JwtService;
import com.bankcore.user.domain.User;
import com.bankcore.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    CustomerProfileRepository customerRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    AuditService auditService;
    @InjectMocks
    AuthService authService;

    @Test
    void loginAuthenticatesAndIssuesTokens() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@bankcore.local")
                .passwordHash("hash")
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmailIgnoreCase("admin@bankcore.local")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
        Instant expiresAt = Instant.now().plusSeconds(1800);
        when(jwtService.accessExpiresAt()).thenReturn(expiresAt);

        LoginResponse response = authService.login(new LoginRequest("admin@bankcore.local", "Password123!"));

        verify(authenticationManager).authenticate(any());
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.role()).isEqualTo(UserRole.ROLE_ADMIN);
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void registerCustomerCreatesPendingKycProfileAndIssuesTokens() {
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(customerRepository.save(any(CustomerProfile.class))).thenAnswer(invocation -> {
            CustomerProfile customer = invocation.getArgument(0);
            customer.setId(UUID.randomUUID());
            return customer;
        });
        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");
        when(jwtService.accessExpiresAt()).thenReturn(Instant.now().plusSeconds(1800));

        LoginResponse response = authService.registerCustomer(new RegisterCustomerRequest(
                "new@example.com",
                "Password123!",
                "New",
                "Customer",
                LocalDate.of(1995, 1, 1),
                "+41790000000",
                "Street 1",
                null,
                "1000",
                "Lausanne",
                "Switzerland"
        ));

        assertThat(response.accessToken()).isEqualTo("access");
        org.mockito.ArgumentCaptor<CustomerProfile> captor = org.mockito.ArgumentCaptor.forClass(CustomerProfile.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getKycStatus()).isEqualTo(KycStatus.PENDING);
    }

    @Test
    void refreshRequiresRefreshTokenTypeAndActiveUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("alice@bankcore.local")
                .passwordHash("hash")
                .role(UserRole.ROLE_CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        when(jwtService.isTokenType("refresh-token", "refresh")).thenReturn(true);
        when(jwtService.subject("refresh-token")).thenReturn("alice@bankcore.local");
        when(userRepository.findByEmailIgnoreCase("alice@bankcore.local")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
        when(jwtService.accessExpiresAt()).thenReturn(Instant.now().plusSeconds(1800));

        LoginResponse response = authService.refresh(new RefreshTokenRequest("refresh-token"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.role()).isEqualTo(UserRole.ROLE_CUSTOMER);
    }

    @Test
    void meReturnsAuthenticatedUserAndCustomerId() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("alice@bankcore.local")
                .passwordHash("hash")
                .role(UserRole.ROLE_CUSTOMER)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(Instant.now())
                .build();
        CustomerProfile customer = CustomerProfile.builder().id(customerId).user(user).build();
        when(currentUserService.currentUser()).thenReturn(new AuthenticatedUser(userId, "alice@bankcore.local", "hash", UserRole.ROLE_CUSTOMER, UserStatus.ACTIVE));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(userId)).thenReturn(Optional.of(customer));

        var response = authService.me();

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.customerId()).isEqualTo(customerId);
    }
}
