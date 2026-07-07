package com.bankcore.security;

import com.bankcore.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret);
    }

    public String generateAccessToken(User user) {
        Instant expiresAt = accessExpiresAt();
        return buildToken(user, expiresAt, "access");
    }

    public String generateRefreshToken(User user) {
        Instant expiresAt = Instant.now().plus(properties.refreshExpirationDays(), ChronoUnit.DAYS);
        return buildToken(user, expiresAt, "refresh");
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String subject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenType(String token, String expectedType) {
        Object type = parseClaims(token).get("typ");
        return expectedType.equals(type);
    }

    public Instant accessExpiresAt() {
        return Instant.now().plus(properties.accessExpirationMinutes(), ChronoUnit.MINUTES);
    }

    private String buildToken(User user, Instant expiresAt, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of(
                        "uid", user.getId().toString(),
                        "role", user.getRole().name(),
                        "typ", tokenType
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }
}
