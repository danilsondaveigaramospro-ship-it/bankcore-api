package com.bankcore.customer.dto;

import com.bankcore.common.enums.KycStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String city,
        String country,
        KycStatus kycStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
