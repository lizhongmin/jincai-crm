package com.jincai.crm.order.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crm_order_traveler")
public class OrderTravelerSnapshot extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "traveler_id", nullable = false)
    private String travelerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_no")
    private String idNo;

    private String phone;
}
