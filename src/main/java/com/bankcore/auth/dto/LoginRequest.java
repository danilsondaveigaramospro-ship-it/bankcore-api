package com.bankcore.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email @NotBlank @Schema(example = "admin@bankcore.local") String email,
        @NotBlank @Schema(example = "Password123!") String password
) {
}
