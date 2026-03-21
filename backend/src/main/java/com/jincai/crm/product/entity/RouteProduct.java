package com.jincai.crm.product.entity;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.repository.*;
import com.jincai.crm.product.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_route")
public class RouteProduct extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "departure_city")
    private String departureCity;

    @Column(name = "destination_city")
    private String destinationCity;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "duration_nights")
    private Integer durationNights;

    private String transportation;

    @Column(name = "hotel_standard")
    private String hotelStandard;

    @Column(name = "highlights", length = 2000)
    private String highlights;

    @Column(name = "fee_includes", length = 2000)
    private String feeIncludes;

    @Column(name = "fee_excludes", length = 2000)
    private String feeExcludes;

    @Column(name = "booking_notice", length = 2000)
    private String bookingNotice;

    @Column(length = 2000)
    private String description;
}

