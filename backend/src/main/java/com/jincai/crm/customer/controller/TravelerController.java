package com.jincai.crm.customer.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.customer.dto.TravelerRequest;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.service.CustomerService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class TravelerController {

    private final CustomerService customerService;

    public TravelerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 分页查询出行人（含数据权限过滤）
     */
    @GetMapping("/travelers/page")
    @PreAuthorize("hasAuthority('MENU_TRAVELER_MANAGE')")
    public ApiResponse<PageResult<Traveler>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String customerId
    ) {
        log.info("请求出行人分页列表 - 用户ID: {}, 页码: {}, 大小: {}, 关键词: {}, 客户ID: {}",
                SecurityUtils.currentUserId(), page, size, keyword, customerId);
        try {
            LoginUser user = SecurityUtils.currentUser();
            PageResult<Traveler> result = customerService.pageTravelersVisible(user, page, size, keyword, customerId);
            log.info("成功获取出行人分页列表 - 第{}页, 共{}条记录", result.page(), result.total());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取出行人分页列表失败 - 页码: {}, 大小: {}, 关键词: {}, 客户ID: {}", page, size, keyword, customerId, e);
            throw e;
        }
    }

    /**
     * 列表查询出行人（按客户 ID 过滤，无数据权限，用于下拉选择等场景）
     */
    @GetMapping({"/travelers", "/customers/{pathCustomerId}/travelers"})
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<Traveler>> list(
        @PathVariable(required = false) String pathCustomerId,
        @RequestParam(required = false) String customerId
    ) {
        String finalCustomerId = pathCustomerId != null ? pathCustomerId : customerId;
        log.info("请求出行人列表 - 用户ID: {}, 客户ID: {}", SecurityUtils.currentUserId(), finalCustomerId);
        try {
            List<Traveler> result = customerService.listTravelersVisible(finalCustomerId);
            log.info("成功获取出行人列表 - 返回 {} 条记录", result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取出行人列表失败 - 客户ID: {}", finalCustomerId, e);
            throw e;
        }
    }

    /**
     * 新增出行人（绑定到指定客户）
     */
    @PostMapping({"/travelers", "/customers/{pathCustomerId}/travelers"})
    @PreAuthorize("hasAuthority('BTN_TRAVELER_CREATE')")
    public ApiResponse<Traveler> create(
        @PathVariable(required = false) String pathCustomerId,
        @RequestParam(required = false) String customerId,
        @Valid @RequestBody TravelerRequest request
    ) {
        String finalCustomerId = pathCustomerId != null ? pathCustomerId : customerId;
        log.info("创建出行人请求 - 用户ID: {}, 客户ID: {}, 出行人姓名: {}, 电话: {}",
                SecurityUtils.currentUserId(), finalCustomerId, request.name(), request.phone());
        try {
            if (finalCustomerId == null) {
                throw new IllegalArgumentException("客户ID不能为空");
            }
            Traveler result = customerService.addTraveler(finalCustomerId, request);
            log.info("成功创建出行人 - 出行人ID: {}, 出行人姓名: {}", result.getId(), result.getName());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建出行人失败 - 客户ID: {}, 出行人姓名: {}", finalCustomerId, request.name(), e);
            throw e;
        }
    }

    /**
     * 更新出行人信息
     */
    @PutMapping({"/travelers/{travelerId}", "/customers/travelers/{travelerId}"})
    @PreAuthorize("hasAuthority('BTN_TRAVELER_EDIT')")
    public ApiResponse<Traveler> update(
        @PathVariable String travelerId,
        @Valid @RequestBody TravelerRequest request
    ) {
        log.info("更新出行人请求 - 用户ID: {}, 出行人ID: {}, 出行人姓名: {}, 电话: {}",
                SecurityUtils.currentUserId(), travelerId, request.name(), request.phone());
        try {
            Traveler result = customerService.updateTraveler(travelerId, request);
            log.info("成功更新出行人 - 出行人ID: {}, 出行人姓名: {}", travelerId, result.getName());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("更新出行人失败 - 出行人ID: {}", travelerId, e);
            throw e;
        }
    }

    /**
     * 删除出行人
     */
    @DeleteMapping({"/travelers/{travelerId}", "/customers/travelers/{travelerId}"})
    @PreAuthorize("hasAuthority('BTN_TRAVELER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String travelerId) {
        log.info("删除出行人请求 - 用户ID: {}, 出行人ID: {}", SecurityUtils.currentUserId(), travelerId);
        try {
            customerService.deleteTraveler(travelerId);
            log.info("成功删除出行人 - 出行人ID: {}", travelerId);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("删除出行人失败 - 出行人ID: {}", travelerId, e);
            throw e;
        }
    }
}
