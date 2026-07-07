package com.bankcore.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FreezeAccountRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
