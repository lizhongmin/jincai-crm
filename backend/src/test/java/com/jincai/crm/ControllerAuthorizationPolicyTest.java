package com.jincai.crm;

import com.jincai.crm.customer.controller.CustomerController;
import com.jincai.crm.finance.controller.FinanceController;
import com.jincai.crm.order.controller.OrderController;
import com.jincai.crm.product.controller.ProductController;
import com.jincai.crm.workflow.controller.WorkflowTemplateController;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ControllerAuthorizationPolicyTest {

    @Test
    void financeRoleShouldBeAbleToReadCustomerRouteAndDepartureLists() throws Exception {
        assertPreAuthorize(CustomerController.class.getMethod("list"), "hasAuthority('MENU_CUSTOMER')");
        assertPreAuthorize(CustomerController.class.getMethod("ownerOptions"), "hasAuthority('MENU_CUSTOMER')");
        assertPreAuthorize(ProductController.class.getMethod("routes"), "hasAuthority('MENU_PRODUCT')");
        assertPreAuthorize(ProductController.class.getMethod("departures", Long.class), "hasAuthority('MENU_PRODUCT')");
        assertPreAuthorize(OrderController.class.getMethod("contextOptions"), "hasAuthority('MENU_ORDER')");
        assertPreAuthorize(OrderController.class.getMethod("customerTravelers", Long.class), "hasAuthority('MENU_ORDER')");
        assertPreAuthorize(OrderController.class.getMethod("departurePrices", Long.class), "hasAuthority('MENU_ORDER')");
        assertPreAuthorize(WorkflowTemplateController.class.getMethod("contextOptions"), "hasAuthority('MENU_WORKFLOW')");
        assertPreAuthorize(FinanceController.class.getMethod("orderOptions"), "hasAuthority('MENU_FINANCE')");
    }

    @Test
    void writeEndpointsShouldRemainRestrictedToSalesOrAdmin() throws Exception {
        assertPreAuthorize(
            CustomerController.class.getMethod("create", com.jincai.crm.customer.dto.CustomerRequest.class),
            "hasAuthority('BTN_CUSTOMER_CREATE')"
        );
        assertPreAuthorize(
            ProductController.class.getMethod("createRoute", com.jincai.crm.product.dto.RouteRequest.class),
            "hasAuthority('BTN_PRODUCT_ROUTE_CREATE')"
        );
        assertPreAuthorize(
            ProductController.class.getMethod("createDeparture", com.jincai.crm.product.dto.DepartureRequest.class),
            "hasAuthority('BTN_PRODUCT_DEPARTURE_CREATE')"
        );
    }

    private void assertPreAuthorize(Method method, String expectedValue) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        Assertions.assertNotNull(preAuthorize, () -> method + " should declare @PreAuthorize");
        Assertions.assertEquals(expectedValue, preAuthorize.value(), () -> method + " has unexpected authorization rule");
    }
}
