package com.jincai.crm.system.entity;

import com.jincai.crm.common.BaseEntity;
import com.jincai.crm.common.DataScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rbac_role")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", nullable = false)
    private DataScope dataScope = DataScope.SELF;
}

