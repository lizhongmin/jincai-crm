package com.jincai.crm.file.entity;

import com.jincai.crm.file.config.*;
import com.jincai.crm.file.controller.*;
import com.jincai.crm.file.repository.*;
import com.jincai.crm.file.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "file_attachment")
public class Attachment extends BaseEntity {

    @Column(name = "biz_type", nullable = false)
    private String bizType;

    @Column(name = "biz_id", nullable = false)
    private Long bizId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;
}

