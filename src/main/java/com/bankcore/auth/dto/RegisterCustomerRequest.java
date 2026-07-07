package com.bankcore.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterCustomerRequest(
        @Email @NotBlank @Schema(example = "new.customer@example.com") String email,
        @NotBlank @Size(min = 10, max = 120) @Schema(example = "Password123!") String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @Pattern(regexp = "^\\+?[0-9 .-]{7,30}$", message = "must be a valid phone number")
        String phoneNumber,
        @NotBlank @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @NotBlank @Size(max = 30) String postalCode,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String country
) {
}
