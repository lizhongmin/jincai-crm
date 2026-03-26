package com.jincai.crm.system.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "org_department")
public class Department extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "tree_path")
    private String treePath;

    @Column(name = "leader_user_id")
    private String leaderUserId;
}

