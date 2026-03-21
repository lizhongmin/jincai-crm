package com.jincai.crm.org.dto;

import com.jincai.crm.org.entity.Permission;
import java.util.List;

public record PermissionTreeGroupView(
    String moduleCode,
    String moduleName,
    List<Permission> permissions
) {
}

