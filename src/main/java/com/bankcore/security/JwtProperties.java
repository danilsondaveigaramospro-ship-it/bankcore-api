package com.bankcore.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bankcore.jwt")
public record JwtProperties(
        String secret,
        long accessExpirationMinutes,
        long refreshExpirationDays
) {
}
