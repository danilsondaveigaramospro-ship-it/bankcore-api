package com.bankcore.customer;

import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.dto.CreateCustomerRequest;
import com.bankcore.customer.dto.UpdateCustomerProfileRequest;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.customer.service.CustomerService;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.user.domain.User;
import com.bankcore.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    CustomerProfileRepository customerRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    AuditService auditService;
    @InjectMocks
    CustomerService customerService;

    @Test
    void employeeCreatesCustomer() {
        when(userRepository.existsByEmailIgnoreCase("client@example.com")).thenReturn(false);
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
        when(currentUserService.currentUser()).thenReturn(employee());

        CustomerProfile customer = customerService.createCustomer(createRequest());

        assertThat(customer.getUser().getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(customer.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
        verify(auditService).record(any(), any(), any(), any(), any());
    }

    @Test
    void customerListReturnsOnlyOwnProfile() {
        AuthenticatedUser user = customer(UUID.randomUUID());
        CustomerProfile ownProfile = CustomerProfile.builder().id(UUID.randomUUID()).user(User.builder().id(user.id()).build()).build();
        when(currentUserService.currentUser()).thenReturn(user);
        when(customerRepository.findByUser_Id(user.id())).thenReturn(Optional.of(ownProfile));

        List<CustomerProfile> result = customerService.listCustomers();

        assertThat(result).containsExactly(ownProfile);
    }

    @Test
    void updateProfileChangesOnlyProvidedFields() {
        UUID customerId = UUID.randomUUID();
        CustomerProfile customer = CustomerProfile.builder()
                .id(customerId)
                .phoneNumber("+41790000000")
                .addressLine1("Old")
                .postalCode("1000")
                .city("Lausanne")
                .country("Switzerland")
                .build();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(currentUserService.currentUser()).thenReturn(employee());

        customerService.updateProfile(customerId, new UpdateCustomerProfileRequest("+41791112233", "New", null, null, "Geneva", null));

        assertThat(customer.getPhoneNumber()).isEqualTo("+41791112233");
        assertThat(customer.getAddressLine1()).isEqualTo("New");
        assertThat(customer.getPostalCode()).isEqualTo("1000");
        assertThat(customer.getCity()).isEqualTo("Geneva");
    }

    private CreateCustomerRequest createRequest() {
        return new CreateCustomerRequest(
                "client@example.com",
                "Password123!",
                "Client",
                "Example",
                LocalDate.of(1990, 1, 1),
                "+41790000000",
                "Street 1",
                null,
                "1000",
                "Lausanne",
                "Switzerland",
                KycStatus.VERIFIED
        );
    }

    private AuthenticatedUser employee() {
        return new AuthenticatedUser(UUID.randomUUID(), "employee@bankcore.local", "hash", UserRole.ROLE_BANK_EMPLOYEE, UserStatus.ACTIVE);
    }

    private AuthenticatedUser customer(UUID id) {
        return new AuthenticatedUser(id, "alice@bankcore.local", "hash", UserRole.ROLE_CUSTOMER, UserStatus.ACTIVE);
    }
}
