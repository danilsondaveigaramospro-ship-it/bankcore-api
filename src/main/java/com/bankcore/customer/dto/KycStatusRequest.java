package com.bankcore.customer.dto;

import com.bankcore.common.enums.KycStatus;
import jakarta.validation.constraints.NotNull;

public record KycStatusRequest(
        @NotNull KycStatus kycStatus
) {
}
