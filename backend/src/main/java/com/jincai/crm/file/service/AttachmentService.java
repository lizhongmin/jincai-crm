package com.jincai.crm.file.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.file.config.FileProperties;
import com.jincai.crm.file.entity.Attachment;
import com.jincai.crm.file.repository.AttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AttachmentService {

    private final AttachmentRepository repository;
    private final FileProperties properties;

    public AttachmentService(AttachmentRepository repository, FileProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public Attachment upload(String bizType, Long bizId, MultipartFile file) {
        log.info("上传附件 - 业务类型: {}, 业务ID: {}, 文件名: {}, 文件大小: {}",
                bizType, bizId, file.getOriginalFilename(), file.getSize());
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                throw new BusinessException("error.file.empty");
            }

            // 获取原始文件名并验证
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new BusinessException("error.file.invalidName");
            }

            // 防止路径遍历攻击
            if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
                throw new BusinessException("error.file.invalidName");
            }

            // 获取文件扩展名
            String extension = getFileExtension(originalFilename);

            // 验证文件扩展名
            if (!isAllowedExtension(extension)) {
                throw new BusinessException("error.file.typeNotAllowed");
            }

            Path root = Path.of(properties.uploadDir());
            Files.createDirectories(root);

            // 生成安全的文件名：UUID + 扩展名
            String filename = UUID.randomUUID() + "." + extension;
            Path target = root.resolve(filename).normalize();

            // 使用 try-with-resources 确保输入流被关闭
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            Attachment attachment = new Attachment();
            attachment.setBizType(bizType);
            attachment.setBizId(bizId);
            attachment.setFileName(originalFilename);  // 保存原始文件名用于显示
            attachment.setFilePath(target.toString());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            Attachment saved = repository.save(attachment);
            log.info("附件上传成功 - 附件ID: {}, 文件路径: {}", saved.getId(), saved.getFilePath());
            return saved;
        } catch (IOException ex) {
            log.error("附件上传失败 - 业务类型: {}, 业务ID: {}, 文件名: {}", bizType, bizId, file.getOriginalFilename(), ex);
            throw new BusinessException("error.file.uploadFailed", ex.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 检查文件扩展名是否允许
     */
    private boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        
        // 允许的常见安全文件类型
        String[] allowedExtensions = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "zip", "rar", "7z"
        };
        
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public List<Attachment> list(String bizType, Long bizId) {
        return repository.findByBizTypeAndBizIdAndDeletedFalse(bizType, bizId);
    }

    public Resource download(String id) {
        Attachment attachment = repository.findById(id).orElseThrow(() -> new BusinessException("error.attachment.notFound"));
        return new FileSystemResource(attachment.getFilePath());
    }
}


