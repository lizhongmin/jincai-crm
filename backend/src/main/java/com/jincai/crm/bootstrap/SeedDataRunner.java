package com.jincai.crm.bootstrap;

import com.jincai.crm.common.DataScope;
import com.jincai.crm.org.entity.AppUser;
import com.jincai.crm.org.entity.Department;
import com.jincai.crm.org.repository.AppUserRepository;
import com.jincai.crm.org.repository.DepartmentRepository;
import com.jincai.crm.org.entity.Permission;
import com.jincai.crm.org.entity.Role;
import com.jincai.crm.org.entity.RolePermission;
import com.jincai.crm.org.entity.UserRole;
import com.jincai.crm.org.repository.PermissionRepository;
import com.jincai.crm.org.repository.RolePermissionRepository;
import com.jincai.crm.org.repository.RoleRepository;
import com.jincai.crm.org.repository.UserRoleRepository;
import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedDataRunner implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(DepartmentRepository departmentRepository, RoleRepository roleRepository,
                          PermissionRepository permissionRepository, AppUserRepository userRepository,
                          UserRoleRepository userRoleRepository, RolePermissionRepository rolePermissionRepository,
                          PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Department headOffice = departmentRepository.findByDeletedFalse().stream().findFirst().orElseGet(() -> {
            Department d = new Department();
            d.setName("Head Office");
            d.setTreePath("/");
            return departmentRepository.save(d);
        });

        Role adminRole = ensureRole("ADMIN", "Admin", "System admin");
        Role salesManagerRole = ensureRole("SALES_MANAGER", "Sales Manager", "Sales team manager");
        Role salesRole = ensureRole("SALES", "Sales", "Sales staff");
        Role financeRole = ensureRole("FINANCE", "Finance", "Finance reviewer");

        Permission menuDashboard = ensurePermission("MENU_DASHBOARD", "Dashboard", "MENU", "/dashboard", null);
        Permission menuOrg = ensurePermission("MENU_ORG", "Org", "MENU", "/org", null);
        Permission menuCustomer = ensurePermission("MENU_CUSTOMER", "Customer", "MENU", "/customers", null);
        Permission menuProduct = ensurePermission("MENU_PRODUCT", "Product", "MENU", "/products", null);
        Permission menuWorkflow = ensurePermission("MENU_WORKFLOW", "Workflow", "MENU", "/workflow", null);
        Permission menuOrder = ensurePermission("MENU_ORDER", "Order", "MENU", "/orders", null);
        Permission menuFinance = ensurePermission("MENU_FINANCE", "Finance", "MENU", "/finance", null);
        Permission menuReport = ensurePermission("MENU_REPORT", "Report", "MENU", "/reports", null);

        List<Permission> permissions = List.of(
            menuDashboard,
            menuOrg,
            menuCustomer,
            menuProduct,
            menuWorkflow,
            menuOrder,
            menuFinance,
            menuReport,
            ensurePermission("BTN_ORG_DEPARTMENT_CREATE", "Create Department", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_DEPARTMENT_EDIT", "Edit Department", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_DEPARTMENT_DELETE", "Delete Department", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_CREATE", "Create User", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_EDIT", "Edit User", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_DELETE", "Delete User", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_STATUS", "Enable Or Disable User", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_RESET_PASSWORD", "Reset Password", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_CREATE", "Create Role", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_EDIT", "Edit Role", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_DELETE", "Delete Role", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_GRANT", "Grant Role", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_CUSTOMER_CREATE", "Create Customer", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_EDIT", "Edit Customer", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_DELETE", "Delete Customer", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_IMPORT", "Import Customer", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_CREATE", "Create Traveler", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_EDIT", "Edit Traveler", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_DELETE", "Delete Traveler", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_PRODUCT_ROUTE_CREATE", "Create Route", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_ROUTE_EDIT", "Edit Route", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_ROUTE_DELETE", "Delete Route", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_CREATE", "Create Departure", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_EDIT", "Edit Departure", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_DELETE", "Delete Departure", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_CREATE", "Create Price", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_EDIT", "Edit Price", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_DELETE", "Delete Price", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_WORKFLOW_CREATE", "Create Workflow", "BUTTON", "", menuWorkflow.getId()),
            ensurePermission("BTN_WORKFLOW_EDIT", "Edit Workflow", "BUTTON", "", menuWorkflow.getId()),
            ensurePermission("BTN_ORDER_CREATE", "Create Order", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_EDIT", "Edit Order", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_DELETE", "Delete Order", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_SUBMIT", "Submit Order", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_APPROVE", "Approve Order", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_REJECT", "Reject Order", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_FINANCE_RECEIVABLE_CREATE", "Create Receivable", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_RECEIPT_CREATE", "Create Receipt", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_REFUND", "Create Refund", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_PAYABLE_CREATE", "Create Payable", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_PAYMENT_CREATE", "Create Payment", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_REVIEW", "Finance Review", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_REPORT_EXPORT", "Export Report", "BUTTON", "", menuReport.getId()),
            ensurePermission("BTN_FILE_UPLOAD", "Upload File", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_AUDIT_VIEW", "View Audit", "BUTTON", "", menuOrder.getId())
        );

        bindRolePermissions(adminRole, permissions);
        bindRolePermissions(financeRole, permissions.stream()
            .filter(p -> p.getCode().contains("FINANCE")
                || p.getCode().contains("REPORT")
                || p.getCode().contains("DASHBOARD")
                || "MENU_ORDER".equals(p.getCode()))
            .toList());
        bindRolePermissions(salesManagerRole, permissions.stream()
            .filter(p -> p.getCode().contains("CUSTOMER")
                || p.getCode().contains("TRAVELER")
                || p.getCode().contains("ORDER")
                || p.getCode().contains("PRODUCT")
                || p.getCode().contains("WORKFLOW")
                || p.getCode().contains("DASHBOARD")
                || p.getCode().contains("REPORT"))
            .toList());
        bindRolePermissions(salesRole, permissions.stream()
            .filter(p -> p.getCode().contains("CUSTOMER")
                || p.getCode().contains("TRAVELER")
                || p.getCode().contains("DASHBOARD")
                || "MENU_ORDER".equals(p.getCode())
                || "BTN_ORDER_CREATE".equals(p.getCode())
                || "BTN_ORDER_EDIT".equals(p.getCode())
                || "BTN_ORDER_SUBMIT".equals(p.getCode()))
            .toList());

        AppUser admin = userRepository.findByUsernameAndDeletedFalse("admin").orElseGet(() -> {
            AppUser user = new AppUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("Admin@123"));
            user.setFullName("System Admin");
            user.setPhone("13800000000");
            user.setDepartmentId(headOffice.getId());
            user.setDataScope(DataScope.ALL);
            user.setEnabled(true);
            return userRepository.save(user);
        });

        if (admin.getPhone() == null || admin.getPhone().isBlank()) {
            admin.setPhone("13800000000");
            userRepository.save(admin);
        }

        boolean hasAdminRole = userRoleRepository.findByUserIdAndDeletedFalse(admin.getId()).stream()
            .anyMatch(r -> r.getRoleId().equals(adminRole.getId()));
        if (!hasAdminRole) {
            UserRole ur = new UserRole();
            ur.setUserId(admin.getId());
            ur.setRoleId(adminRole.getId());
            userRoleRepository.save(ur);
        }
    }

    private Role ensureRole(String code, String name, String desc) {
        return roleRepository.findByCodeAndDeletedFalse(code).orElseGet(() -> {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(desc);
            return roleRepository.save(role);
        });
    }

    private Permission ensurePermission(String code, String name, String type, String menuPath, Long parentId) {
        Permission permission = permissionRepository.findByCodeAndDeletedFalse(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setCode(code);
            p.setName(name);
            p.setType(type);
            p.setMenuPath(menuPath);
            p.setParentId(parentId);
            return permissionRepository.save(p);
        });
        boolean changed = false;
        if (!java.util.Objects.equals(parentId, permission.getParentId())) {
            permission.setParentId(parentId);
            changed = true;
        }
        if (menuPath != null && !menuPath.equals(permission.getMenuPath())) {
            permission.setMenuPath(menuPath);
            changed = true;
        }
        if (changed) {
            permission = permissionRepository.save(permission);
        }
        return permission;
    }

    private void bindRolePermissions(Role role, List<Permission> permissions) {
        List<RolePermission> existing = rolePermissionRepository.findByRoleIdAndDeletedFalse(role.getId());
        Set<Long> existingPermissionIds = existing.stream()
            .map(RolePermission::getPermissionId)
            .collect(java.util.stream.Collectors.toSet());
        permissions.forEach(permission -> {
            if (!existingPermissionIds.contains(permission.getId())) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permission.getId());
                rolePermissionRepository.save(rp);
            }
        });
    }
}
