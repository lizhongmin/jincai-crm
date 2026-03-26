package com.jincai.crm.system.dto;

import com.jincai.crm.system.entity.Permission;

import java.util.List;

/**
 * 权限树分组视图（3级结构）
 * <pre>
 * 1级：moduleCode/moduleName —— 顶级菜单（分组标题）
 * 2级：subMenus             —— 子菜单列表（每个子菜单含自身权限点）
 * 3级：subMenu.actions      —— 该子菜单下的权限点（BUTTON/API）
 * </pre>
 * 若某顶级菜单本身没有子菜单（扁平结构），则 subMenus 中包含一个
 * moduleName 与顶级一致的虚拟分组，直接存放顶级菜单自身的权限点。
 */
public record PermissionTreeGroupView(
    String moduleCode,
    String moduleName,
    /** 顶级菜单本身是否是一个可授权节点 */
    Permission menuPermission,
    List<SubMenuView> subMenus
) {
    public record SubMenuView(
        String code,
        String name,
        /** 子菜单自身的 Permission 记录（type=MENU） */
        Permission menuPermission,
        /** 该子菜单下的权限点（BUTTON / API 等） */
        List<Permission> actions
    ) {}
}
