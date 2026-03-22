package com.jincai.crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "finance", authorities = {"MENU_CUSTOMER", "MENU_PRODUCT"})
    void financeShouldAccessCommonReadApis() throws Exception {
        mockMvc.perform(get("/customers")).andExpect(status().isOk());
        mockMvc.perform(get("/customers/owner-options")).andExpect(status().isOk());
        mockMvc.perform(get("/routes")).andExpect(status().isOk());
        mockMvc.perform(get("/departures")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "finance", authorities = {"MENU_CUSTOMER"})
    void financeShouldNotAccessCustomerWriteApi() throws Exception {
        mockMvc.perform(delete("/customers/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "sales", authorities = {"MENU_ORDER"})
    void orderUserShouldAccessOrderContextOptions() throws Exception {
        mockMvc.perform(get("/orders/context-options")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "workflow", authorities = {"MENU_WORKFLOW"})
    void workflowUserShouldAccessWorkflowContextOptions() throws Exception {
        mockMvc.perform(get("/workflow/templates/context-options")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "finance", authorities = {"MENU_FINANCE"})
    void financeUserShouldAccessFinanceOrderOptions() throws Exception {
        mockMvc.perform(get("/finance/orders/options")).andExpect(status().isOk());
    }
}
