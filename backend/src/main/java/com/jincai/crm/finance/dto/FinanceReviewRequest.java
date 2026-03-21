package com.jincai.crm.finance.dto;

import com.jincai.crm.finance.controller.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.repository.*;
import com.jincai.crm.finance.service.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinanceReviewRequest(
    @NotBlank String targetType,
    @NotNull Boolean approved,
    String comment
) {
}

