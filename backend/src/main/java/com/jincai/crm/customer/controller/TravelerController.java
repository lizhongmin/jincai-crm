package com.jincai.crm.customer.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.customer.dto.TravelerRequest;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.service.CustomerService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/travelers")
public class TravelerController {

    private final CustomerService customerService;

    public TravelerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 分页查询出行人（含数据权限过滤）
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<PageResult<Traveler>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String customerId
    ) {
        LoginUser user = SecurityUtils.currentUser();
        return ApiResponse.ok(customerService.pageTravelersVisible(user, page, size, keyword, customerId));
    }

    /**
     * 列表查询出行人（按客户 ID 过滤，无数据权限，用于下拉选择等场景）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<Traveler>> list(@RequestParam(required = false) String customerId) {
        return ApiResponse.ok(customerService.listTravelersVisible(customerId));
    }

    /**
     * 新增出行人（绑定到指定客户）
     */
    @PostMapping
    @PreAuthorize("hasAuthority('BTN_TRAVELER_CREATE')")
    public ApiResponse<Traveler> create(
        @RequestParam String customerId,
        @Valid @RequestBody TravelerRequest request
    ) {
        return ApiResponse.ok(customerService.addTraveler(customerId, request));
    }

    /**
     * 更新出行人信息
     */
    @PutMapping("/{travelerId}")
    @PreAuthorize("hasAuthority('BTN_TRAVELER_EDIT')")
    public ApiResponse<Traveler> update(
        @PathVariable String travelerId,
        @Valid @RequestBody TravelerRequest request
    ) {
        return ApiResponse.ok(customerService.updateTraveler(travelerId, request));
    }

    /**
     * 删除出行人
     */
    @DeleteMapping("/{travelerId}")
    @PreAuthorize("hasAuthority('BTN_TRAVELER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String travelerId) {
        customerService.deleteTraveler(travelerId);
        return ApiResponse.ok(null);
    }
}
