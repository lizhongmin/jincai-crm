package com.jincai.crm.workflow.dto;

import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.system.entity.Role;

import java.util.List;

public record WorkflowContextOptionsView(
    List<Role> roles,
    List<RouteProduct> routes,
    List<Departure> departures
) {
}
