package com.jincai.crm.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nService i18nService;

    public GlobalExceptionHandler(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        String message = i18nService.getMessage(ex.getMessageKey(), ex.getMessageArgs());
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        String message = i18nService.getMessage("common.validation.failed");
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraint(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .map(v -> v.getMessage())
            .distinct()
            .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = i18nService.getMessage("common.validation.failed");
        }
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        String message = i18nService.getMessage("common.auth.badCredentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(message));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
        String message = i18nService.getMessage("common.auth.accountDisabled");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException ex) {
        String message = i18nService.getMessage("common.auth.forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        String message = i18nService.getMessage("common.server.error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(message));
    }
}

