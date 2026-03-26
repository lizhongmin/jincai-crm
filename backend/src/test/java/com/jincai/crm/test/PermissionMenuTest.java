package com.jincai.crm.test;

import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PermissionMenuTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    public void testPermissionMenus() {
        // 查看所有权限菜单
        List<Permission> permissions = permissionRepository.findByDeletedFalse();

        System.out.println("=== 所有权限菜单 ===");
        for (Permission p : permissions) {
            System.out.println("Code: " + p.getCode() +
                             ", Name: " + p.getName() +
                             ", Type: " + p.getType() +
                             ", MenuPath: '" + p.getMenuPath() + "'" +
                             ", ParentId: " + p.getParentId());
        }

        // 查看系统管理相关权限
        System.out.println("\n=== 系统管理相关权限 ===");
        permissions.stream()
            .filter(p -> p.getCode().contains("MENU_ORG") || p.getParentId() != null)
            .forEach(p -> {
                System.out.println("Code: " + p.getCode() +
                                 ", Name: " + p.getName() +
                                 ", MenuPath: '" + p.getMenuPath() + "'" +
                                 ", ParentId: " + p.getParentId());
            });
    }
}