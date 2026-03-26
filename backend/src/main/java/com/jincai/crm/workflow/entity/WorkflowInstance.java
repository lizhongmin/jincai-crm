package com.jincai.crm.workflow.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_instance")
public class WorkflowInstance extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep;

    @Column(nullable = false)
    private String status;
}

