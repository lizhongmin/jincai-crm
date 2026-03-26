package com.jincai.crm.file.repository;

import com.jincai.crm.file.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByBizTypeAndBizIdAndDeletedFalse(String bizType, Long bizId);
}

