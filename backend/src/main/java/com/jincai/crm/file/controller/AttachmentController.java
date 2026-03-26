package com.jincai.crm.file.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.file.entity.Attachment;
import com.jincai.crm.file.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('BTN_FILE_UPLOAD')")
    public ApiResponse<Attachment> upload(@RequestParam("bizType") String bizType,
                                          @RequestParam("bizId") Long bizId,
                                          @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(service.upload(bizType, bizId, file));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MENU_ORDER','MENU_CUSTOMER','MENU_PRODUCT','MENU_FINANCE')")
    public ApiResponse<java.util.List<Attachment>> list(@RequestParam("bizType") String bizType,
                                                         @RequestParam("bizId") Long bizId) {
        return ApiResponse.ok(service.list(bizType, bizId));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('MENU_ORDER','MENU_CUSTOMER','MENU_PRODUCT','MENU_FINANCE')")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        Resource resource = service.download(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
