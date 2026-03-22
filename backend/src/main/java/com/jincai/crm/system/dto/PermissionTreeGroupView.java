package com.jincai.crm.system.dto;

import com.jincai.crm.system.entity.Permission;
import java.util.List;

public record PermissionTreeGroupView(
    String moduleCode,
    String moduleName,
    List<Permission> permissions
) {
}

