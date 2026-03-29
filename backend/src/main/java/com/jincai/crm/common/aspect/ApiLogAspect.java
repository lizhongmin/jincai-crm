package com.jincai.crm.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jincai.crm.audit.entity.ApiAuditLog;
import com.jincai.crm.audit.repository.ApiAuditLogRepository;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 接口请求与响应日志打印切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiLogAspect {

    private final ObjectMapper objectMapper;
    private final ApiAuditLogRepository apiAuditLogRepository;
    @Qualifier("auditLogExecutor")
    private final TaskExecutor auditLogExecutor;

    private static final String TRACE_ID = "traceId";
    private static final String TRACE_HEADER_NAME = "X-Trace-Id";

    /** 需要记录响应体的写操作 HTTP 方法 */
    private static final java.util.Set<String> WRITE_METHODS = java.util.Set.of("POST", "PUT", "PATCH", "DELETE");

    /**
     * 定义切点为所有 Controller 的方法
     */
    @Pointcut("execution(* com.jincai.crm..controller..*.*(..))")
    public void controllerLog() {
    }

    @Around("controllerLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID, traceId);

        if (attributes == null) {
            try {
                return joinPoint.proceed();
            } finally {
                MDC.remove(TRACE_ID);
            }
        }

        HttpServletResponse response = attributes.getResponse();
        if (response != null) {
            response.setHeader(TRACE_HEADER_NAME, traceId);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();

        ApiAuditLog apiLog = new ApiAuditLog();
        apiLog.setTraceId(traceId);
        apiLog.setRequestUrl(request.getRequestURI());
        apiLog.setHttpMethod(request.getMethod());
        apiLog.setClassMethod(methodName);
        apiLog.setSourceIp(extractClientIp(request));
        fillOperatorInfo(apiLog);

        // 过滤掉不能被序列化的请求参数
        Object[] args = joinPoint.getArgs();
        List<Object> validArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                || arg instanceof MultipartFile || arg instanceof MultipartFile[]) {
                continue;
            }
            validArgs.add(arg);
        }

        try {
            if (!validArgs.isEmpty()) {
                apiLog.setRequestArgs(objectMapper.writeValueAsString(validArgs));
            } else {
                apiLog.setRequestArgs("[]");
            }
        } catch (Exception e) {
            apiLog.setRequestArgs("[Error parsing args: " + e.getMessage() + "]");
        }

        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Throwable t) {
            apiLog.setResponseResult("[Exception: " + t.getMessage() + "]");
            throw t;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            apiLog.setTimeConsuming(executionTime);

            // 仅写操作（POST/PUT/PATCH/DELETE）才记录响应体，查询请求跳过
            boolean isWriteOp = WRITE_METHODS.contains(request.getMethod().toUpperCase());
            if (isWriteOp && result != null) {
                try {
                    apiLog.setResponseResult(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    apiLog.setResponseResult("[Error parsing response: " + e.getMessage() + "]");
                }
            }

            if (response != null) {
                apiLog.setHttpStatus(response.getStatus());
            }

            // 输出组装好的日志审计对象
            try {
                log.info("API Audit Log: {}", objectMapper.writeValueAsString(apiLog));
            } catch (Exception e) {
                log.error("Failed to serialize API Audit Log", e);
            }

            // 将当前线程 MDC 上下文传入异步线程，保证 traceId 在子线程可用
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            auditLogExecutor.execute(() -> {
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                try {
                    apiAuditLogRepository.save(apiLog);
                } catch (Exception e) {
                    log.error("Failed to save API Audit Log to database", e);
                } finally {
                    MDC.clear();
                }
            });

            MDC.remove(TRACE_ID);
        }

        return result;
    }

    private void fillOperatorInfo(ApiAuditLog apiLog) {
        LoginUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null) {
            apiLog.setCreatedBy("ANONYMOUS");
            return;
        }
        apiLog.setCreatedBy(currentUser.getUsername());
    }

    private String extractClientIp(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "X-Real-IP"
        };
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String resolveTraceId(HttpServletRequest request) {
        if (request == null) {
            return generateTraceId();
        }
        String traceIdFromRequest = request.getHeader(TRACE_HEADER_NAME);
        if (traceIdFromRequest == null || traceIdFromRequest.isBlank()) {
            return generateTraceId();
        }
        return traceIdFromRequest.trim();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

