package com.jincai.crm.order.dto;

import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.RouteProduct;

import java.util.List;

public record OrderContextOptionsView(
    List<Customer> customers,
    List<RouteProduct> routes,
    List<Departure> departures
) {
}
