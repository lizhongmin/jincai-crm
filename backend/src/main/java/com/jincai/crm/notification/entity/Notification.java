package com.jincai.crm.notification.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_notification")
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Boolean readFlag = false;
}

