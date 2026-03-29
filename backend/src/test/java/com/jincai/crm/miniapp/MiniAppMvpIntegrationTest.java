package com.jincai.crm.miniapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jincai.crm.auth.dto.LoginStateResponse;
import com.jincai.crm.auth.service.LoginSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.miniapp.mock-enabled=true"
})
class MiniAppMvpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginSecurityService loginSecurityService;

    @BeforeEach
    void setUp() {
        when(loginSecurityService.getLoginState(any())).thenReturn(new LoginStateResponse(false, false, 0));
        when(loginSecurityService.onLoginFailure(any())).thenReturn(new LoginStateResponse(false, false, 0));
        doNothing().when(loginSecurityService).onLoginSuccess(any());
        doNothing().when(loginSecurityService).ensureLoginAllowedAndValidateCaptcha(any(), any(), any());
    }

    @Test
    void shouldReturnUnboundResultForUnknownMiniAppIdentity() throws Exception {
        mockMvc.perform(post("/miniapp/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "mock:unbound-sales"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(false))
            .andExpect(jsonPath("$.data.token").value(nullValue()))
            .andExpect(jsonPath("$.data.username").value(nullValue()));
    }

    @Test
    void shouldBindMiniAppIdentityAndSupportFollowUpLoginAndStatus() throws Exception {
        MvcResult bindResult = mockMvc.perform(post("/miniapp/auth/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "mock:bound-admin",
                      "username": "admin",
                      "password": "Admin@123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.token").isString())
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andReturn();

        String bindToken = readToken(bindResult);

        MvcResult loginResult = mockMvc.perform(post("/miniapp/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "mock:bound-admin"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.token").isString())
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andReturn();

        String loginToken = readToken(loginResult);

        mockMvc.perform(get("/miniapp/auth/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.username").value("admin"));

        mockMvc.perform(get("/miniapp/auth/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bindToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void shouldCreateAndReadCustomerThroughMiniAppLoginToken() throws Exception {
        String token = bindMiniAppIdentity("mock:bound-admin");

        MvcResult createResult = mockMvc.perform(post("/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "小程序客户",
                      "phone": "13900000001",
                      "customerType": "PERSONAL",
                      "source": "ONLINE",
                      "intentionLevel": "HIGH",
                      "remark": "来自小程序"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("小程序客户"))
            .andExpect(jsonPath("$.data.phone").value("13900000001"))
            .andReturn();

        String customerId = readDataField(createResult, "id");

        mockMvc.perform(get("/customers/{id}", customerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(customerId))
            .andExpect(jsonPath("$.data.name").value("小程序客户"))
            .andExpect(jsonPath("$.data.phone").value("13900000001"))
            .andExpect(jsonPath("$.data.source").value("ONLINE"))
            .andExpect(jsonPath("$.data.intentionLevel").value("HIGH"))
            .andExpect(jsonPath("$.data.remark").value("来自小程序"));
    }

    private String bindMiniAppIdentity(String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/miniapp/auth/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "%s",
                      "username": "admin",
                      "password": "Admin@123"
                    }
                    """.formatted(code)))
            .andExpect(status().isOk())
            .andReturn();
        return readToken(result);
    }

    private String readToken(MvcResult result) throws Exception {
        return readDataField(result, "token");
    }

    private String readDataField(MvcResult result, String fieldName) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode value = root.path("data").path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
