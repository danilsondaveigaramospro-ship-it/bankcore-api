package com.bankcore.customer.mapper;

import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.dto.CustomerResponse;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(CustomerProfile customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getUser().getId(),
                customer.getUser().getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDateOfBirth(),
                customer.getPhoneNumber(),
                customer.getAddressLine1(),
                customer.getAddressLine2(),
                customer.getPostalCode(),
                customer.getCity(),
                customer.getCountry(),
                customer.getKycStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
