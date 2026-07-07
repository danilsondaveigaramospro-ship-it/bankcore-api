package com.bankcore.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;

@Component
public class ReferenceGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(String prefix) {
        StringBuilder builder = new StringBuilder(prefix)
                .append('-')
                .append(Instant.now().toEpochMilli())
                .append('-');
        for (int i = 0; i < 8; i++) {
            builder.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }
}
