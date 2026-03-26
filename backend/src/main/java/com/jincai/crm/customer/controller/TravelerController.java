package com.jincai.crm.customer.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.service.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/travelers")
public class TravelerController {

    private final CustomerService customerService;

    public TravelerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<Traveler>> list(@RequestParam(value = "customerId", required = false) String customerId) {
        return ApiResponse.ok(customerService.listTravelersVisible(customerId));
    }
}
