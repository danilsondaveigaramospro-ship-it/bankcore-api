package com.bankcore.customer.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerProfileRequest(
        @Pattern(regexp = "^\\+?[0-9 .-]{7,30}$", message = "must be a valid phone number")
        String phoneNumber,
        @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @Size(max = 30) String postalCode,
        @Size(max = 100) String city,
        @Size(max = 100) String country
) {
}
