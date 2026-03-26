package com.jincai.crm.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(length = 26, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId = "jincai";

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UlidGenerator.generate();
        }
    }
}

