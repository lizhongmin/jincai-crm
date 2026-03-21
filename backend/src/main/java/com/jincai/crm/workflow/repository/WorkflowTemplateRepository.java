package com.jincai.crm.workflow.repository;

import com.jincai.crm.workflow.controller.*;
import com.jincai.crm.workflow.dto.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {

    List<WorkflowTemplate> findByActiveTrueAndDeletedFalse();
}

