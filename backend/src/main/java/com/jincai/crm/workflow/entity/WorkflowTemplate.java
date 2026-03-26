package com.jincai.crm.workflow.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "workflow_template")
public class WorkflowTemplate extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(name = "product_category", nullable = false)
    private String productCategory;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "departure_id")
    private String departureId;

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private Boolean active = true;
}
