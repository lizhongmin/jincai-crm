package com.jincai.crm.workflow.entity;

import com.jincai.crm.workflow.controller.*;
import com.jincai.crm.workflow.dto.*;
import com.jincai.crm.workflow.repository.*;
import com.jincai.crm.workflow.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private Boolean active = true;
}

