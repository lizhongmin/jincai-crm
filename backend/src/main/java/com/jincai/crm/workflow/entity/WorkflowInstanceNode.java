package com.jincai.crm.workflow.entity;

import com.jincai.crm.workflow.controller.*;
import com.jincai.crm.workflow.dto.*;
import com.jincai.crm.workflow.repository.*;
import com.jincai.crm.workflow.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_instance_node")
public class WorkflowInstanceNode extends BaseEntity {

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "node_name", nullable = false)
    private String nodeName;

    @Column(name = "approver_role_code", nullable = false)
    private String approverRoleCode;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String comment;
}

