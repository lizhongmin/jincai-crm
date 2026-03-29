package com.jincai.crm.miniapp.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "miniapp_user_binding",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_miniapp_user_binding_user", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_miniapp_user_binding_open", columnNames = "open_id")
    }
)
public class MiniAppUserBinding extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "open_id", nullable = false, length = 128)
    private String openId;

    @Column(name = "union_id", length = 128)
    private String unionId;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "bound_at", nullable = false)
    private LocalDateTime boundAt;
}
