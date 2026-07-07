package com.bankcore.auth.dto;

import com.bankcore.common.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant expiresAt,
        UUID userId,
        String email,
        UserRole role
) {
}
