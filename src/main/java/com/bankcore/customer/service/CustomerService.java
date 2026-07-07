package com.bankcore.customer.service;

import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;
import com.bankcore.common.exception.DuplicateResourceException;
import com.bankcore.common.exception.ResourceNotFoundException;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.dto.CreateCustomerRequest;
import com.bankcore.customer.dto.UpdateCustomerProfileRequest;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import com.bankcore.user.domain.User;
import com.bankcore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerProfileRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional
    public CustomerProfile createCustomer(CreateCustomerRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
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
                .kycStatus(request.kycStatus() == null ? KycStatus.PENDING : request.kycStatus())
                .build();
        customerRepository.save(customer);
        auditService.record(currentUserService.currentUser().id(), "CUSTOMER_CREATED", "CustomerProfile", customer.getId().toString(), "{}");
        return customer;
    }

    @Transactional(readOnly = true)
    public List<CustomerProfile> listCustomers() {
        AuthenticatedUser user = currentUserService.currentUser();
        if (user.role() == UserRole.ROLE_CUSTOMER) {
            return customerRepository.findByUser_Id(user.id()).stream().toList();
        }
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CustomerProfile getCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional(readOnly = true)
    public CustomerProfile getCurrentCustomer() {
        UUID userId = currentUserService.currentUser().id();
        return customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
    }

    @Transactional
    public CustomerProfile updateKycStatus(UUID customerId, KycStatus status) {
        CustomerProfile customer = getCustomer(customerId);
        customer.setKycStatus(status);
        auditService.record(currentUserService.currentUser().id(), "CUSTOMER_KYC_UPDATED", "CustomerProfile", customerId.toString(), "{\"kycStatus\":\"" + status + "\"}");
        return customer;
    }

    @Transactional
    public CustomerProfile updateProfile(UUID customerId, UpdateCustomerProfileRequest request) {
        CustomerProfile customer = getCustomer(customerId);
        if (request.phoneNumber() != null) {
            customer.setPhoneNumber(request.phoneNumber());
        }
        if (request.addressLine1() != null) {
            customer.setAddressLine1(request.addressLine1());
        }
        if (request.addressLine2() != null) {
            customer.setAddressLine2(request.addressLine2());
        }
        if (request.postalCode() != null) {
            customer.setPostalCode(request.postalCode());
        }
        if (request.city() != null) {
            customer.setCity(request.city());
        }
        if (request.country() != null) {
            customer.setCountry(request.country());
        }
        auditService.record(currentUserService.currentUser().id(), "CUSTOMER_PROFILE_UPDATED", "CustomerProfile", customerId.toString(), "{}");
        return customer;
    }
}
