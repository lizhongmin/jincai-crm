package com.jincai.crm.customer.entity;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

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
@Table(name = "crm_traveler")
public class Traveler extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_no")
    private String idNo;

    private LocalDate birthday;

    private String phone;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "preferences", length = 1000)
    private String preferences;
}

