package com.jincai.crm.miniapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.miniapp.MiniAppProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class MiniAppWechatService {

    private final MiniAppProperties miniAppProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MiniAppWechatService(MiniAppProperties miniAppProperties) {
        this.miniAppProperties = miniAppProperties;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.weixin.qq.com")
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public WechatSession exchangeCode(String code) {
        String normalizedCode = code == null ? "" : code.trim();
        if (normalizedCode.isEmpty()) {
            throw new BusinessException("error.miniapp.code.required");
        }

        String mockPrefix = miniAppProperties.mockCodePrefix() == null || miniAppProperties.mockCodePrefix().isBlank()
            ? "mock:"
            : miniAppProperties.mockCodePrefix();
        if (miniAppProperties.mockEnabled() && normalizedCode.startsWith(mockPrefix)) {
            String mockOpenId = normalizedCode.substring(mockPrefix.length()).trim();
            if (mockOpenId.isEmpty()) {
                throw new BusinessException("error.miniapp.wechat.mockCode.invalid");
            }
            return new WechatSession(mockOpenId, null);
        }

        if (isBlank(miniAppProperties.appId()) || isBlank(miniAppProperties.secret())) {
            throw new BusinessException("error.miniapp.wechat.configMissing");
        }

        // 微信 API 返回 text/plain 格式，需要手动解析 JSON 字符串
        String responseBody = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/sns/jscode2session")
                .queryParam("appid", miniAppProperties.appId())
                .queryParam("secret", miniAppProperties.secret())
                .queryParam("js_code", normalizedCode)
                .queryParam("grant_type", "authorization_code")
                .build())
            .retrieve()
            .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException("error.miniapp.wechat.exchangeFailed", "empty response");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response;
        try {
            response = objectMapper.readValue(responseBody, Map.class);
        } catch (Exception e) {
            throw new BusinessException("error.miniapp.wechat.parseFailed", "Failed to parse response: " + e.getMessage());
        }

        if (response == null) {
            throw new BusinessException("error.miniapp.wechat.exchangeFailed", "empty response");
        }
        Object errorCode = response.get("errcode");
        if (errorCode != null && !"0".equals(String.valueOf(errorCode))) {
            throw new BusinessException("error.miniapp.wechat.exchangeFailed", response.getOrDefault("errmsg", errorCode));
        }

        String openId = toText(response.get("openid"));
        if (openId == null || openId.isBlank()) {
            throw new BusinessException("error.miniapp.wechat.exchangeFailed", "missing openid");
        }
        return new WechatSession(openId, toText(response.get("unionid")));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record WechatSession(String openId, String unionId) {
    }
}
