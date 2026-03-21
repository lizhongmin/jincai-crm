package com.jincai.crm.file.repository;

import com.jincai.crm.file.config.*;
import com.jincai.crm.file.controller.*;
import com.jincai.crm.file.entity.*;
import com.jincai.crm.file.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByBizTypeAndBizIdAndDeletedFalse(String bizType, Long bizId);
}

