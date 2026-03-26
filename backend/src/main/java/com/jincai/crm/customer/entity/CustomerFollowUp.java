package com.jincai.crm.customer.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crm_customer_follow_up")
public class CustomerFollowUp extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;
}

