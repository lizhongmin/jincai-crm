package com.jincai.crm.audit.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "api_audit_log")
public class ApiAuditLog extends BaseEntity {

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "http_method", length = 20)
    private String httpMethod;

    @Column(name = "class_method", length = 200)
    private String classMethod;

    @Column(name = "source_ip", length = 50)
    private String sourceIp;

    @Column(name = "request_args", columnDefinition = "TEXT")
    private String requestArgs;

    @Column(name = "response_result", columnDefinition = "TEXT")
    private String responseResult;

    @Column(name = "time_consuming")
    private Long timeConsuming;
}
