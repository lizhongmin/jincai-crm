package com.jincai.crm.file.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.file.entity.Attachment;
import com.jincai.crm.file.service.AttachmentService;
import com.jincai.crm.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@Slf4j
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('BTN_FILE_UPLOAD')")
    public ApiResponse<Attachment> upload(@RequestParam("bizType") String bizType,
                                          @RequestParam("bizId") String bizId,
                                          @RequestParam("file") MultipartFile file) {
        log.info("AttachmentController.upload() called by user: {}, bizType: {}, bizId: {}, filename: {}", SecurityUtils.currentUserId(), bizType, bizId, file.getOriginalFilename());
        try {
            Attachment result = service.upload(bizType, bizId, file);
            log.info("AttachmentController.upload() succeeded for user: {}, attachment ID: {}", SecurityUtils.currentUserId(), result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("AttachmentController.upload() failed for user: {}, bizType: {}, bizId: {}", SecurityUtils.currentUserId(), bizType, bizId, e);
            throw e;
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MENU_ORDER','MENU_CUSTOMER','MENU_PRODUCT','MENU_FINANCE')")
    public ApiResponse<java.util.List<Attachment>> list(@RequestParam("bizType") String bizType,
                                                         @RequestParam("bizId") String bizId) {
        log.debug("AttachmentController.list() called by user: {}, bizType: {}, bizId: {}", SecurityUtils.currentUserId(), bizType, bizId);
        try {
            java.util.List<Attachment> result = service.list(bizType, bizId);
            log.debug("AttachmentController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("AttachmentController.list() failed for user: {}, bizType: {}, bizId: {}", SecurityUtils.currentUserId(), bizType, bizId, e);
            throw e;
        }
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('MENU_ORDER','MENU_CUSTOMER','MENU_PRODUCT','MENU_FINANCE')")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        log.info("AttachmentController.download() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            Resource resource = service.download(id);
            log.info("AttachmentController.download() succeeded for user: {}, id: {}", SecurityUtils.currentUserId(), id);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (Exception e) {
            log.error("AttachmentController.download() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
