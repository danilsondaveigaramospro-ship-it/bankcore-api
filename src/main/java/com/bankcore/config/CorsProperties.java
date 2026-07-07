package com.bankcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "bankcore.cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
}
