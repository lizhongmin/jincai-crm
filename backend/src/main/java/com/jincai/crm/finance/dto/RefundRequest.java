package com.jincai.crm.finance.dto;

import com.jincai.crm.finance.controller.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.repository.*;
import com.jincai.crm.finance.service.*;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RefundRequest(
    @NotNull Long orderId,
    @NotNull BigDecimal amount,
    String reason
) {
}

