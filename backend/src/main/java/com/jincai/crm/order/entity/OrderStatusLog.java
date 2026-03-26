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
@Table(name = "crm_order_status_log")
public class OrderStatusLog extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "from_status", nullable = false)
    private String fromStatus;

    @Column(name = "to_status", nullable = false)
    private String toStatus;

    @Column(name = "remark", length = 1000)
    private String remark;
}

