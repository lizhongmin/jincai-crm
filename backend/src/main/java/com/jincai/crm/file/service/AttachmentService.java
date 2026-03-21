package com.jincai.crm.file.service;

import com.jincai.crm.file.config.*;
import com.jincai.crm.file.controller.*;
import com.jincai.crm.file.entity.*;
import com.jincai.crm.file.repository.*;

import com.jincai.crm.common.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

    private final AttachmentRepository repository;
    private final FileProperties properties;

    public AttachmentService(AttachmentRepository repository, FileProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public Attachment upload(String bizType, Long bizId, MultipartFile file) {
        try {
            Path root = Path.of(properties.uploadDir());
            Files.createDirectories(root);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = root.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = new Attachment();
            attachment.setBizType(bizType);
            attachment.setBizId(bizId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(target.toString());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            return repository.save(attachment);
        } catch (IOException ex) {
            throw new BusinessException("File upload failed: " + ex.getMessage());
        }
    }

    public List<Attachment> list(String bizType, Long bizId) {
        return repository.findByBizTypeAndBizIdAndDeletedFalse(bizType, bizId);
    }

    public Resource download(Long id) {
        Attachment attachment = repository.findById(id).orElseThrow(() -> new BusinessException("Attachment not found"));
        return new FileSystemResource(attachment.getFilePath());
    }
}

