package com.jincai.crm.audit.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "before_value", length = 2000)
    private String beforeValue;

    @Column(name = "after_value", length = 2000)
    private String afterValue;

    @Column(name = "source_ip")
    private String sourceIp;
}

