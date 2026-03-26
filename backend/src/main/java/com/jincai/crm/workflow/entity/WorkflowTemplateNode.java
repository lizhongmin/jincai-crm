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
@Table(name = "workflow_template_node")
public class WorkflowTemplateNode extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "node_name", nullable = false)
    private String nodeName;

    @Column(name = "approver_role_code", nullable = false)
    private String approverRoleCode;
}

