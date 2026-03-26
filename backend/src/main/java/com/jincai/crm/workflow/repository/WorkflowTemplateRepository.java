package com.jincai.crm.workflow.repository;

import com.jincai.crm.workflow.entity.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, String>, JpaSpecificationExecutor<WorkflowTemplate> {

    List<WorkflowTemplate> findByActiveTrueAndDeletedFalse();
}
