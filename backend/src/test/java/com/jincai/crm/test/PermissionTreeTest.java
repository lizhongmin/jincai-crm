package com.jincai.crm.test;

import com.jincai.crm.system.dto.PermissionTreeView;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PermissionTreeTest {

    @Autowired
    private PermissionService permissionService;

    @Test
    public void testPermissionTree() {
        // 获取权限树视图
        List<PermissionTreeView> tree = permissionService.treeView();

        System.out.println("=== 权限树结构 ===");
        printTree(tree, 0);
    }

    private void printTree(List<PermissionTreeView> nodes, int level) {
        String indent = "  ".repeat(level);
        for (PermissionTreeView node : nodes) {
            System.out.println(indent + "- " + node.name() + " (" + node.code() + ")");
            if (node.children() != null && !node.children().isEmpty()) {
                printTree(node.children(), level + 1);
            }
        }
    }
}