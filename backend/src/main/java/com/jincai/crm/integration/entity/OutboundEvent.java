package com.jincai.crm.integration.entity;

import com.jincai.crm.integration.repository.*;
import com.jincai.crm.integration.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "integration_outbound_event")
public class OutboundEvent extends BaseEntity {

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "biz_type", nullable = false)
    private String bizType;

    @Column(name = "biz_id", nullable = false)
    private Long bizId;

    @Column(name = "payload", nullable = false, length = 4000)
    private String payload;

    @Column(name = "status", nullable = false)
    private String status = "NEW";
}

