package com.jincai.crm.customer.entity;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crm_customer")
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(name = "customer_type")
    private String customerType;

    @Column(name = "source_channel")
    private String source;

    @Column(name = "intention_level")
    private String intentionLevel;

    @Column(name = "customer_status")
    private String status;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "owner_dept_id", nullable = false)
    private Long ownerDeptId;

    @Column(name = "customer_level")
    private String level;

    private String wechat;

    private String email;

    private String city;

    @Column(length = 1000)
    private String tags;

    @Column(length = 2000)
    private String remark;
}

