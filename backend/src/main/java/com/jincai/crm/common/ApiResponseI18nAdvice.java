package com.jincai.crm.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseI18nAdvice implements ResponseBodyAdvice<Object> {

    private final I18nService i18nService;

    public ApiResponseI18nAdvice(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof ApiResponse<?> apiResponse)) {
            return body;
        }
        String message = apiResponse.message();
        if (message == null || message.isBlank()) {
            return body;
        }
        String key = "OK".equals(message) ? "common.ok" : (isMessageKey(message) ? message : null);
        if (key == null) {
            return body;
        }
        return new ApiResponse<>(apiResponse.success(), i18nService.getMessage(key), apiResponse.data());
    }

    private boolean isMessageKey(String message) {
        return message.startsWith("common.") || message.startsWith("error.") || message.startsWith("validation.");
    }
}
