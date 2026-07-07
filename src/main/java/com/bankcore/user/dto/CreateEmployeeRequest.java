package com.bankcore.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 10, max = 120) String password
) {
}
