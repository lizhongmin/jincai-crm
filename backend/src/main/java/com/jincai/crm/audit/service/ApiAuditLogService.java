package com.jincai.crm.audit.service;

import com.jincai.crm.audit.entity.ApiAuditLog;
import com.jincai.crm.audit.repository.ApiAuditLogRepository;
import com.jincai.crm.common.PageResult;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApiAuditLogService {

    private final ApiAuditLogRepository repository;

    public ApiAuditLogService(ApiAuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * 分页查询 API 审计日志（无条件）。
     */
    public PageResult<ApiAuditLog> page(int page, int size) {
        return page(page, size, null, null, null, null);
    }

    /**
     * 分页查询 API 审计日志（带条件）。
     *
     * @param page       页码（从 1 开始）
     * @param size       每页大小
     * @param keyword    关键词（匹配 traceId / requestUrl / classMethod）
     * @param operator   执行人
     * @param httpMethod 请求方式
     * @param startTime  起始时间（含）
     * @param endTime    结束时间（含）
     */
    public PageResult<ApiAuditLog> page(int page, int size, String keyword, String operator, String httpMethod, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("分页查询API审计日志 - 页码: {}, 大小: {}, 关键词: {}, 执行人: {}, 请求方式: {}, 起始: {}, 结束: {}", page, size, keyword, operator, httpMethod, startTime, endTime);
        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt"));

            Specification<ApiAuditLog> spec = buildSpec(keyword, operator, httpMethod, startTime, endTime);
            Page<ApiAuditLog> pageData = repository.findAll(spec, pageable);

            log.debug("API审计日志查询完成 - 总记录: {}", pageData.getTotalElements());
            return new PageResult<>(pageData.getContent(), pageData.getTotalElements(), page, size);
        } catch (Exception e) {
            log.error("查询API审计日志失败 - 页码: {}, 大小: {}", page, size, e);
            throw e;
        }
    }

    private Specification<ApiAuditLog> buildSpec(String keyword, String operator, String httpMethod, LocalDateTime startTime, LocalDateTime endTime) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("traceId")), like),
                    cb.like(cb.lower(root.get("requestUrl")), like),
                    cb.like(cb.lower(root.get("classMethod")), like)
                ));
            }

            if (operator != null && !operator.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("createdBy")), "%" + operator.trim().toLowerCase() + "%"));
            }

            if (httpMethod != null && !httpMethod.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("httpMethod")), httpMethod.trim().toUpperCase()));
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
