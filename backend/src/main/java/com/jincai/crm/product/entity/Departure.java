package com.jincai.crm.product.entity;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.repository.*;
import com.jincai.crm.product.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_departure")
public class Departure extends BaseEntity {

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "min_group_size")
    private Integer minGroupSize;

    @Column(name = "max_group_size")
    private Integer maxGroupSize;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "gathering_place")
    private String gatheringPlace;

    @Column(name = "departure_notice", length = 2000)
    private String departureNotice;
}
