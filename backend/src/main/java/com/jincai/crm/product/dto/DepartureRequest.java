package com.jincai.crm.product.dto;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.repository.*;
import com.jincai.crm.product.service.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepartureRequest(
    @NotNull Long routeId,
    String code,
    @NotBlank String startDate,
    @NotBlank String endDate,
    @NotNull @Min(0) Integer stock,
    String registrationDeadline,
    @Min(0) Integer minGroupSize,
    @Min(0) Integer maxGroupSize,
    String status,
    String gatheringPlace,
    String departureNotice
) {
}
