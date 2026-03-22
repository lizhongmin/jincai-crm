package com.jincai.crm.bootstrap;

import com.jincai.crm.common.DataScope;
import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.entity.RolePermission;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.repository.RolePermissionRepository;
import com.jincai.crm.system.repository.RoleRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
        Department headOffice = ensureHeadOffice();

        Role adminRole = ensureRole("ADMIN", "管理员", "系统管理员角色");
        Role salesManagerRole = ensureRole("SALES_MANAGER", "销售经理", "销售团队负责人");
        Role salesRole = ensureRole("SALES", "销售", "销售人员");
        Role financeRole = ensureRole("FINANCE", "财务", "财务审核人员");

        Permission menuDashboard = ensurePermission("MENU_DASHBOARD", "经营看板", "MENU", "/dashboard", null);
        Permission menuOrg = ensurePermission("MENU_ORG", "系统管理", "MENU", "/org", null);
        Permission menuSecurity = ensurePermission("MENU_SECURITY", "登录安全", "MENU", "/security", null);
        Permission menuCustomer = ensurePermission("MENU_CUSTOMER", "客户管理", "MENU", "/customers", null);
        Permission menuProduct = ensurePermission("MENU_PRODUCT", "产品团期", "MENU", "/products", null);
        Permission menuWorkflow = ensurePermission("MENU_WORKFLOW", "流程模板", "MENU", "/workflow", null);
        Permission menuOrder = ensurePermission("MENU_ORDER", "订单管理", "MENU", "/orders", null);
        Permission menuFinance = ensurePermission("MENU_FINANCE", "收付审核", "MENU", "/finance", null);
        Permission menuReport = ensurePermission("MENU_REPORT", "BI报表", "MENU", "/reports", null);

        List<Permission> permissions = List.of(
            menuDashboard,
            menuOrg,
            menuSecurity,
            menuCustomer,
            menuProduct,
            menuWorkflow,
            menuOrder,
            menuFinance,
            menuReport,

            ensurePermission("BTN_DASHBOARD_VIEW", "看板查看", "BUTTON", "", menuDashboard.getId()),

            ensurePermission("BTN_ORG_DEPARTMENT_VIEW", "部门查看", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_DEPARTMENT_CREATE", "部门新增", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_DEPARTMENT_EDIT", "部门编辑", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_DEPARTMENT_DELETE", "部门删除", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_VIEW", "用户查看", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_CREATE", "用户新增", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_EDIT", "用户编辑", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_DELETE", "用户删除", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_STATUS", "用户启停", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_USER_RESET_PASSWORD", "重置密码", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_VIEW", "角色查看", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_CREATE", "角色新增", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_EDIT", "角色编辑", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_DELETE", "角色删除", "BUTTON", "", menuOrg.getId()),
            ensurePermission("BTN_ORG_ROLE_GRANT", "角色授权", "BUTTON", "", menuOrg.getId()),

            ensurePermission("BTN_SECURITY_POLICY_VIEW", "策略查看", "BUTTON", "", menuSecurity.getId()),
            ensurePermission("BTN_SECURITY_POLICY_EDIT", "策略编辑", "BUTTON", "", menuSecurity.getId()),

            ensurePermission("BTN_CUSTOMER_VIEW", "客户查看", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_CREATE", "客户新增", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_EDIT", "客户编辑", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_DELETE", "客户删除", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_CUSTOMER_IMPORT", "客户导入", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_VIEW", "出行人查看", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_CREATE", "出行人新增", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_EDIT", "出行人编辑", "BUTTON", "", menuCustomer.getId()),
            ensurePermission("BTN_TRAVELER_DELETE", "出行人删除", "BUTTON", "", menuCustomer.getId()),

            ensurePermission("BTN_PRODUCT_ROUTE_VIEW", "线路查看", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_ROUTE_CREATE", "线路新增", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_ROUTE_EDIT", "线路编辑", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_ROUTE_DELETE", "线路删除", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_VIEW", "团期查看", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_CREATE", "团期新增", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_EDIT", "团期编辑", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_DEPARTURE_DELETE", "团期删除", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_VIEW", "价格查看", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_CREATE", "价格新增", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_EDIT", "价格编辑", "BUTTON", "", menuProduct.getId()),
            ensurePermission("BTN_PRODUCT_PRICE_DELETE", "价格删除", "BUTTON", "", menuProduct.getId()),

            ensurePermission("BTN_WORKFLOW_VIEW", "流程查看", "BUTTON", "", menuWorkflow.getId()),
            ensurePermission("BTN_WORKFLOW_CREATE", "流程新增", "BUTTON", "", menuWorkflow.getId()),
            ensurePermission("BTN_WORKFLOW_EDIT", "流程编辑", "BUTTON", "", menuWorkflow.getId()),

            ensurePermission("BTN_ORDER_VIEW", "订单查看", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_CREATE", "订单新增", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_EDIT", "订单编辑", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_DELETE", "订单删除", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_SUBMIT", "订单提交", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_APPROVE", "订单审批", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_ORDER_REJECT", "订单驳回", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_FILE_UPLOAD", "附件上传", "BUTTON", "", menuOrder.getId()),
            ensurePermission("BTN_AUDIT_VIEW", "审计查看", "BUTTON", "", menuOrder.getId()),

            ensurePermission("BTN_FINANCE_RECEIVABLE_VIEW", "应收查看", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_RECEIVABLE_CREATE", "应收新增", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_RECEIPT_VIEW", "收款查看", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_RECEIPT_CREATE", "收款新增", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_REFUND_VIEW", "退款查看", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_REFUND", "退款新增", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_PAYABLE_VIEW", "应付查看", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_PAYABLE_CREATE", "应付新增", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_PAYMENT_VIEW", "付款查看", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_PAYMENT_CREATE", "付款新增", "BUTTON", "", menuFinance.getId()),
            ensurePermission("BTN_FINANCE_REVIEW", "财务审核", "BUTTON", "", menuFinance.getId()),

            ensurePermission("BTN_REPORT_VIEW", "报表查看", "BUTTON", "", menuReport.getId()),
            ensurePermission("BTN_REPORT_EXPORT", "报表导出", "BUTTON", "", menuReport.getId())
        );

        bindRolePermissions(adminRole, permissions);

        bindRolePermissions(financeRole, permissions.stream()
            .filter(p -> p.getCode().startsWith("MENU_FINANCE")
                || p.getCode().startsWith("BTN_FINANCE_")
                || p.getCode().startsWith("MENU_REPORT")
                || p.getCode().startsWith("BTN_REPORT_")
                || p.getCode().startsWith("MENU_DASHBOARD")
                || p.getCode().startsWith("BTN_DASHBOARD_")
                || p.getCode().startsWith("MENU_ORDER")
                || p.getCode().startsWith("BTN_ORDER_VIEW"))
            .toList());

        bindRolePermissions(salesManagerRole, permissions.stream()
            .filter(p -> p.getCode().startsWith("MENU_CUSTOMER")
                || p.getCode().startsWith("BTN_CUSTOMER_")
                || p.getCode().startsWith("BTN_TRAVELER_")
                || p.getCode().startsWith("MENU_ORDER")
                || p.getCode().startsWith("BTN_ORDER_")
                || p.getCode().startsWith("BTN_FILE_UPLOAD")
                || p.getCode().startsWith("MENU_PRODUCT")
                || p.getCode().startsWith("BTN_PRODUCT_")
                || p.getCode().startsWith("MENU_WORKFLOW")
                || p.getCode().startsWith("BTN_WORKFLOW_")
                || p.getCode().startsWith("MENU_DASHBOARD")
                || p.getCode().startsWith("BTN_DASHBOARD_")
                || p.getCode().startsWith("MENU_REPORT")
                || p.getCode().startsWith("BTN_REPORT_VIEW"))
            .toList());

        bindRolePermissions(salesRole, permissions.stream()
            .filter(p -> p.getCode().startsWith("MENU_CUSTOMER")
                || p.getCode().startsWith("BTN_CUSTOMER_VIEW")
                || p.getCode().startsWith("BTN_CUSTOMER_CREATE")
                || p.getCode().startsWith("BTN_CUSTOMER_EDIT")
                || p.getCode().startsWith("BTN_TRAVELER_VIEW")
                || p.getCode().startsWith("BTN_TRAVELER_CREATE")
                || p.getCode().startsWith("BTN_TRAVELER_EDIT")
                || p.getCode().startsWith("MENU_DASHBOARD")
                || p.getCode().startsWith("BTN_DASHBOARD_")
                || p.getCode().startsWith("MENU_ORDER")
                || p.getCode().startsWith("BTN_ORDER_VIEW")
                || p.getCode().startsWith("BTN_ORDER_CREATE")
                || p.getCode().startsWith("BTN_ORDER_EDIT")
                || p.getCode().startsWith("BTN_ORDER_SUBMIT"))
            .toList());

        AppUser admin = userRepository.findByUsernameAndDeletedFalse("admin").orElseGet(() -> {
            AppUser user = new AppUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("Admin@123"));
            user.setFullName("系统管理员");
            user.setPhone("13800000000");
            user.setDepartmentId(headOffice.getId());
            user.setDataScope(DataScope.ALL);
            user.setEnabled(true);
            return userRepository.save(user);
        });

        boolean adminChanged = false;
        if (admin.getPhone() == null || admin.getPhone().isBlank()) {
            admin.setPhone("13800000000");
            adminChanged = true;
        }
        if (!"系统管理员".equals(admin.getFullName())) {
            admin.setFullName("系统管理员");
            adminChanged = true;
        }
        if (adminChanged) {
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
        Role role = roleRepository.findByCodeAndDeletedFalse(code).orElseGet(() -> {
            Role r = new Role();
            r.setCode(code);
            return r;
        });
        boolean changed = false;
        if (!Objects.equals(role.getName(), name)) {
            role.setName(name);
            changed = true;
        }
        if (!Objects.equals(role.getDescription(), desc)) {
            role.setDescription(desc);
            changed = true;
        }
        if (role.getId() == null || changed) {
            role = roleRepository.save(role);
        }
        return role;
    }

    private Department ensureHeadOffice() {
        List<Department> departments = departmentRepository.findByDeletedFalse();
        Department department = departments.stream()
            .filter(item -> "总部".equals(item.getName()))
            .findFirst()
            .orElseGet(() -> departments.stream()
                .filter(item -> item.getParentId() == null)
                .findFirst()
                .orElseGet(Department::new));
        boolean changed = false;
        if (!"总部".equals(department.getName())) {
            department.setName("总部");
            changed = true;
        }
        if (department.getTreePath() == null || department.getTreePath().isBlank()) {
            department.setTreePath("/");
            changed = true;
        }
        if (department.getId() == null || changed) {
            department = departmentRepository.save(department);
        }
        return department;
    }

    private Permission ensurePermission(String code, String name, String type, String menuPath, Long parentId) {
        Permission permission = permissionRepository.findByCodeAndDeletedFalse(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setCode(code);
            return p;
        });
        boolean changed = false;
        if (!Objects.equals(name, permission.getName())) {
            permission.setName(name);
            changed = true;
        }
        if (!Objects.equals(type, permission.getType())) {
            permission.setType(type);
            changed = true;
        }
        if (!Objects.equals(parentId, permission.getParentId())) {
            permission.setParentId(parentId);
            changed = true;
        }
        if (menuPath != null && !menuPath.equals(permission.getMenuPath())) {
            permission.setMenuPath(menuPath);
            changed = true;
        }
        if (permission.getId() == null || changed) {
            permission = permissionRepository.save(permission);
        }
        return permission;
    }

    private void bindRolePermissions(Role role, List<Permission> permissions) {
        List<RolePermission> existing = rolePermissionRepository.findByRoleIdAndDeletedFalse(role.getId());
        Set<Long> existingPermissionIds = existing.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toSet());
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
