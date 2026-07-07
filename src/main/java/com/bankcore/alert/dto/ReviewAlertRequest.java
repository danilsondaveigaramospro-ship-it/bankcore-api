package com.bankcore.alert.dto;

import jakarta.validation.constraints.Size;

public record ReviewAlertRequest(
        @Size(max = 500) String note
) {
}
