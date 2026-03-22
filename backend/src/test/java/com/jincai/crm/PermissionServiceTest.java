package com.jincai.crm;

import com.jincai.crm.system.dto.PermissionTreeGroupView;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.repository.RolePermissionRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import com.jincai.crm.system.service.PermissionService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PermissionServiceTest {

    @Test
    void shouldHandleNullParentIdPermissionsInTree() {
        PermissionRepository permissionRepository = Mockito.mock(PermissionRepository.class);
        UserRoleRepository userRoleRepository = Mockito.mock(UserRoleRepository.class);
        RolePermissionRepository rolePermissionRepository = Mockito.mock(RolePermissionRepository.class);

        Permission menu = new Permission();
        menu.setId(1L);
        menu.setCode("customer");
        menu.setName("Customer");
        menu.setType("MENU");
        menu.setParentId(null);

        Permission topButton = new Permission();
        topButton.setId(2L);
        topButton.setCode("customer:create");
        topButton.setName("Create Customer");
        topButton.setType("BUTTON");
        topButton.setParentId(null);

        Mockito.when(permissionRepository.findByDeletedFalse()).thenReturn(List.of(menu, topButton));

        PermissionService service = new PermissionService(permissionRepository, userRoleRepository, rolePermissionRepository);

        List<PermissionTreeGroupView> tree = Assertions.assertDoesNotThrow(service::tree);
        Assertions.assertTrue(tree.stream().anyMatch(group -> "MISC".equals(group.moduleCode())));
    }

    @Test
    void shouldIgnoreNullPermissionIdWhenBuildingTree() {
        PermissionRepository permissionRepository = Mockito.mock(PermissionRepository.class);
        UserRoleRepository userRoleRepository = Mockito.mock(UserRoleRepository.class);
        RolePermissionRepository rolePermissionRepository = Mockito.mock(RolePermissionRepository.class);

        Permission menuWithNullId = new Permission();
        menuWithNullId.setId(null);
        menuWithNullId.setCode("menu:invalid");
        menuWithNullId.setName("Invalid Menu");
        menuWithNullId.setType("MENU");
        menuWithNullId.setParentId(null);

        Permission rootMenu = new Permission();
        rootMenu.setId(10L);
        rootMenu.setCode("menu:customer");
        rootMenu.setName("Customer");
        rootMenu.setType("MENU");
        rootMenu.setParentId(null);

        Permission childButton = new Permission();
        childButton.setId(11L);
        childButton.setCode("btn:customer:create");
        childButton.setName("Create Customer");
        childButton.setType("BUTTON");
        childButton.setParentId(10L);

        Mockito.when(permissionRepository.findByDeletedFalse()).thenReturn(List.of(menuWithNullId, rootMenu, childButton));

        PermissionService service = new PermissionService(permissionRepository, userRoleRepository, rolePermissionRepository);

        List<PermissionTreeGroupView> tree = Assertions.assertDoesNotThrow(service::tree);
        Assertions.assertFalse(tree.isEmpty());
    }
}
