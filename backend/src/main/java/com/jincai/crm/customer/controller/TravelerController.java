package com.jincai.crm.customer.controller;

import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

import com.jincai.crm.common.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/travelers")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
public class TravelerController {

    private final CustomerService customerService;

    public TravelerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ApiResponse<List<Traveler>> list(@RequestParam(value = "customerId", required = false) Long customerId) {
        return ApiResponse.ok(customerService.listTravelersVisible(customerId));
    }
}

