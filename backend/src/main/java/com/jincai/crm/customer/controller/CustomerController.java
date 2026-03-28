package com.jincai.crm.customer.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.service.CustomerService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<CustomerView>> list() {
        log.info("请求客户列表 - 用户ID: {}", SecurityUtils.currentUserId());
        try {
            LoginUser user = SecurityUtils.currentUser();
            List<CustomerView> result = customerService.listVisible(user);
            log.info("成功获取客户列表 - 返回 {} 条记录", result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取客户列表失败", e);
            throw e;
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<PageResult<CustomerView>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tab,
        @RequestParam(required = false) String ownerScope
    ) {
        log.info("请求客户分页列表 - 用户ID: {}, 页码: {}, 大小: {}, 关键词: {}, 标签: {}, 所有者范围: {}",
                SecurityUtils.currentUserId(), page, size, keyword, tab, ownerScope);
        try {
            LoginUser user = SecurityUtils.currentUser();
            PageResult<CustomerView> result = customerService.pageVisible(user, page, size, keyword, tab, ownerScope);
            log.info("成功获取客户分页列表 - 第{}页, 共{}条记录", result.page(), result.total());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取客户分页列表失败 - 页码: {}, 大小: {}", page, size, e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_CREATE')")
    public ApiResponse<CustomerView> create(@Valid @RequestBody CustomerRequest request) {
        log.info("创建客户请求 - 用户ID: {}, 客户姓名: {}, 电话: {}",
                SecurityUtils.currentUserId(), request.name(), request.phone());
        try {
            CustomerView result = customerService.create(request, SecurityUtils.currentUser());
            log.info("成功创建客户 - 客户ID: {}, 客户姓名: {}", result.id(), result.name());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建客户失败 - 客户姓名: {}", request.name(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_EDIT')")
    public ApiResponse<CustomerView> update(@PathVariable String id, @Valid @RequestBody CustomerRequest request) {
        log.info("更新客户请求 - 用户ID: {}, 客户ID: {}, 客户姓名: {}, 电话: {}",
                SecurityUtils.currentUserId(), id, request.name(), request.phone());
        try {
            CustomerView result = customerService.update(id, request, SecurityUtils.currentUser());
            log.info("成功更新客户 - 客户ID: {}, 客户姓名: {}", id, result.name());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("更新客户失败 - 客户ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("删除客户请求 - 用户ID: {}, 客户ID: {}", SecurityUtils.currentUserId(), id);
        try {
            customerService.delete(id);
            log.info("成功删除客户 - 客户ID: {}", id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("删除客户失败 - 客户ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/owner-options")
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<CustomerOwnerOptionView>> ownerOptions() {
        log.info("请求客户所有者选项列表 - 用户ID: {}", SecurityUtils.currentUserId());
        try {
            List<CustomerOwnerOptionView> result = customerService.listOwnerOptions(SecurityUtils.currentUser());
            log.info("成功获取客户所有者选项列表 - 返回 {} 条记录", result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取客户所有者选项列表失败", e);
            throw e;
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_IMPORT')")
    public ApiResponse<ImportResult> importCustomers(@RequestParam("file") MultipartFile file) {
        log.info("导入客户请求 - 用户ID: {}, 文件名: {}, 文件大小: {}",
                SecurityUtils.currentUserId(), file.getOriginalFilename(), file.getSize());
        try {
            ImportResult result = customerService.importCustomers(file, SecurityUtils.currentUser());
            log.info("成功导入客户 - 成功: {} 条, 失败: {} 条", result.success(), result.errors().size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("导入客户失败 - 文件名: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }
}

