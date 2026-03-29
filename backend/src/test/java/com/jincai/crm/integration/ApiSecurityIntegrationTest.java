package com.jincai.crm.integration;

import com.jincai.crm.auth.dto.LoginStateResponse;
import com.jincai.crm.auth.service.LoginSecurityService;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.customer.service.CustomerService;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.order.service.OrderService;
import com.jincai.crm.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginSecurityService loginSecurityService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        when(loginSecurityService.getLoginState(any())).thenReturn(new LoginStateResponse(false, false, 0));
        when(loginSecurityService.onLoginFailure(any())).thenReturn(new LoginStateResponse(false, false, 0));
        doNothing().when(loginSecurityService).onLoginSuccess(any());
        doNothing().when(loginSecurityService).ensureLoginAllowedAndValidateCaptcha(any(), any(), any());
    }

    @Test
    void shouldReturnUnauthorizedWhenUnauthenticatedUserAccessesProtectedApi() throws Exception {
        mockMvc.perform(get("/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(authorities = {"MENU_CUSTOMER"})
    void shouldReturnForbiddenWhenMissingRequiredAuthority() throws Exception {
        mockMvc.perform(get("/customers/page"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(authorities = {"BTN_CUSTOMER_CREATE"})
    void shouldRejectInvalidCustomerPayload() throws Exception {
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "phone": ""
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(authorities = {"MENU_ORDER"})
    void shouldReturnOrderPageForAuthorizedUser() throws Exception {
        when(orderService.pageVisible(any(LoginUser.class), anyInt(), anyInt(), any(), any(), any()))
            .thenReturn(new PageResult<TravelOrder>(List.of(), 0, 1, 10));

        mockMvc.perform(get("/orders/page"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(authorities = {"MENU_ORDER"})
    void shouldUseClientTraceIdWhenHeaderProvided() throws Exception {
        when(orderService.pageVisible(any(LoginUser.class), anyInt(), anyInt(), any(), any(), any()))
            .thenReturn(new PageResult<TravelOrder>(List.of(), 0, 1, 10));

        mockMvc.perform(get("/orders/page").header("X-Trace-Id", "frontend-trace-id-001"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Trace-Id", "frontend-trace-id-001"));
    }

    @Test
    @WithMockUser(authorities = {"MENU_ORDER"})
    void shouldGenerateTraceIdWhenHeaderMissing() throws Exception {
        when(orderService.pageVisible(any(LoginUser.class), anyInt(), anyInt(), any(), any(), any()))
            .thenReturn(new PageResult<TravelOrder>(List.of(), 0, 1, 10));

        mockMvc.perform(get("/orders/page"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Trace-Id"));
    }
}
