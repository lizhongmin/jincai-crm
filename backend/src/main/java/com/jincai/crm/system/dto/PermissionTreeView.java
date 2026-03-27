package com.jincai.crm.system.dto;

import com.jincai.crm.system.entity.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树视图（用于管理界面展示完整树结构）
 */
public record PermissionTreeView(
    String id,
    String code,
    String name,
    String type,
    String menuPath,
    String parentId,
    Integer sortOrder,
    List<PermissionTreeView> children
) {
    public PermissionTreeView(String id, String code, String name, String type, String menuPath, String parentId, Integer sortOrder) {
        this(id, code, name, type, menuPath, parentId, sortOrder, new ArrayList<>());
    }

    public PermissionTreeView(Permission permission) {
        this(
            permission.getId(),
            permission.getCode(),
            permission.getName(),
            permission.getType(),
            permission.getMenuPath(),
            permission.getParentId(),
            permission.getSortOrder()
        );
    }

    public PermissionTreeView(Permission permission, List<PermissionTreeView> children) {
        this(
            permission.getId(),
            permission.getCode(),
            permission.getName(),
            permission.getType(),
            permission.getMenuPath(),
            permission.getParentId(),
            permission.getSortOrder(),
            children
        );
    }
}
