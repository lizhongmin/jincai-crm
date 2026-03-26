package com.jincai.crm.workflow.repository;

import com.jincai.crm.workflow.entity.WorkflowTemplateNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTemplateNodeRepository extends JpaRepository<WorkflowTemplateNode, String> {

    List<WorkflowTemplateNode> findByTemplateIdAndDeletedFalseOrderByStepOrderAsc(String templateId);

    List<WorkflowTemplateNode> findByTemplateIdAndDeletedFalse(String templateId);

    void deleteByTemplateId(String templateId);
}
