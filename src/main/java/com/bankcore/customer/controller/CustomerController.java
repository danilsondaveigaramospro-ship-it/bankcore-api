package com.bankcore.customer.controller;

import com.bankcore.customer.dto.CreateCustomerRequest;
import com.bankcore.customer.dto.CustomerResponse;
import com.bankcore.customer.dto.KycStatusRequest;
import com.bankcore.customer.dto.UpdateCustomerProfileRequest;
import com.bankcore.customer.mapper.CustomerMapper;
import com.bankcore.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Create a customer profile and customer user")
    public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return CustomerMapper.toResponse(customerService.createCustomer(request));
    }

    @GetMapping
    @Operation(summary = "List customers. Customers receive only their own profile.")
    public List<CustomerResponse> listCustomers() {
        return customerService.listCustomers().stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("@customerAccessPolicy.canAccessCustomer(#customerId)")
    @Operation(summary = "Get a customer profile with ownership checks")
    public CustomerResponse getCustomer(@PathVariable UUID customerId) {
        return CustomerMapper.toResponse(customerService.getCustomer(customerId));
    }

    @PatchMapping("/{customerId}/kyc-status")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Update customer KYC status")
    public CustomerResponse updateKyc(@PathVariable UUID customerId, @Valid @RequestBody KycStatusRequest request) {
        return CustomerMapper.toResponse(customerService.updateKycStatus(customerId, request.kycStatus()));
    }

    @PatchMapping("/{customerId}")
    @PreAuthorize("@customerAccessPolicy.canAccessCustomer(#customerId)")
    @Operation(summary = "Update non-sensitive profile fields")
    public CustomerResponse updateProfile(@PathVariable UUID customerId, @Valid @RequestBody UpdateCustomerProfileRequest request) {
        return CustomerMapper.toResponse(customerService.updateProfile(customerId, request));
    }
}
