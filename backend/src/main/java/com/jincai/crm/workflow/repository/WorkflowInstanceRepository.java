package com.jincai.crm.workflow.repository;

import com.jincai.crm.workflow.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, String> {

    Optional<WorkflowInstance> findByOrderIdAndDeletedFalse(String orderId);
}

