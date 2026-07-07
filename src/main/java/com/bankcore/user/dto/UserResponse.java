package com.bankcore.user.dto;

import com.bankcore.common.enums.UserRole;
import com.bankcore.common.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt
) {
}
