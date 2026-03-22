package com.jincai.crm.customer.controller;

import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
        LoginUser user = SecurityUtils.currentUser();
        return ApiResponse.ok(customerService.listVisible(user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_CREATE')")
    public ApiResponse<CustomerView> create(@Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok(customerService.create(request, SecurityUtils.currentUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_EDIT')")
    public ApiResponse<CustomerView> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok(customerService.update(id, request, SecurityUtils.currentUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/travelers")
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<Traveler>> travelers(@PathVariable Long id) {
        return ApiResponse.ok(customerService.listTravelers(id));
    }

    @GetMapping("/owner-options")
    @PreAuthorize("hasAuthority('MENU_CUSTOMER')")
    public ApiResponse<List<CustomerOwnerOptionView>> ownerOptions() {
        return ApiResponse.ok(customerService.listOwnerOptions(SecurityUtils.currentUser()));
    }

    @PostMapping("/{id}/travelers")
    @PreAuthorize("hasAuthority('BTN_TRAVELER_CREATE')")
    public ApiResponse<Traveler> addTraveler(@PathVariable Long id, @Valid @RequestBody TravelerRequest request) {
        return ApiResponse.ok(customerService.addTraveler(id, request));
    }

    @PutMapping("/travelers/{travelerId}")
    @PreAuthorize("hasAuthority('BTN_TRAVELER_EDIT')")
    public ApiResponse<Traveler> updateTraveler(@PathVariable Long travelerId, @Valid @RequestBody TravelerRequest request) {
        return ApiResponse.ok(customerService.updateTraveler(travelerId, request));
    }

    @DeleteMapping("/travelers/{travelerId}")
    @PreAuthorize("hasAuthority('BTN_TRAVELER_DELETE')")
    public ApiResponse<Void> deleteTraveler(@PathVariable Long travelerId) {
        customerService.deleteTraveler(travelerId);
        return ApiResponse.ok(null);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BTN_CUSTOMER_IMPORT')")
    public ApiResponse<ImportResult> importCustomers(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(customerService.importCustomers(file, SecurityUtils.currentUser()));
    }
}
