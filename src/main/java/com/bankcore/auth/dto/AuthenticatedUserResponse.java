package com.bankcore.auth.dto;

import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String email,
        UserRole role,
        UserStatus status,
        UUID customerId,
        Instant lastLoginAt
) {
}
