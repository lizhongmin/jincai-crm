package com.jincai.crm.workflow.repository;

import com.jincai.crm.workflow.entity.WorkflowInstanceNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceNodeRepository extends JpaRepository<WorkflowInstanceNode, String> {

    List<WorkflowInstanceNode> findByInstanceIdAndDeletedFalseOrderByStepOrderAsc(String instanceId);

    Optional<WorkflowInstanceNode> findByInstanceIdAndStepOrderAndDeletedFalse(String instanceId, Integer stepOrder);
}

