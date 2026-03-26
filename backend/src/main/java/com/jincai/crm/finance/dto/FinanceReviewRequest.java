package com.jincai.crm.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinanceReviewRequest(
    @NotBlank String targetType,
    @NotNull Boolean approved,
    String comment
) {
}

