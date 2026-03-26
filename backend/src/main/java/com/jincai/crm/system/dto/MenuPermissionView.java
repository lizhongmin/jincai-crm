package com.jincai.crm.system.dto;

import com.jincai.crm.system.entity.Permission;

public record MenuPermissionView(
    String code,
    String name,
    String type,
    String menuPath,
    String parentId
) {
    public static MenuPermissionView from(Permission p) {
        return new MenuPermissionView(
            p.getCode(),
            p.getName(),
            p.getType(),
            p.getMenuPath(),
            p.getParentId()
        );
    }
}
