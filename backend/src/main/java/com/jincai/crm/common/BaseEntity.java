package com.jincai.crm.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 所有业务实体的公共基类。
 *
 * <p>提供以下通用能力：
 * <ul>
 *   <li><b>ID 自动生成</b>：采用 ULID，26 位字符串，按时间有序，无需数据库序列。</li>
 *   <li><b>多租户隔离</b>：{@code tenantId} 字段区分不同租户数据，默认为 "jincai"。</li>
 *   <li><b>JPA 审计</b>：自动记录创建人/时间、最后修改人/时间。</li>
 *   <li><b>软删除</b>：{@code deleted = true} 表示逻辑删除，查询时需过滤 {@code deleted = false}。</li>
 *   <li><b>乐观锁</b>：{@code version} 字段防止并发写入时的丢失更新（Lost Update）问题。
 *       当两个事务同时读取同一行并尝试更新时，后提交的事务会收到
 *       {@link jakarta.persistence.OptimisticLockException}，调用方应捕获并重试。</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Entity
 * @Table(name = "crm_order")
 * public class TravelOrder extends BaseEntity {
 *     // 业务字段 ...
 * }
 * }</pre>
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * 主键，采用 ULID（26 位字符串），按创建时间有序。
     * 由 {@link #prePersist()} 在首次持久化前自动填充，不允许更新。
     */
    @Id
    @Column(length = 26, nullable = false, updatable = false)
    private String id;

    /**
     * 租户标识，用于多租户数据隔离。
     * 默认值 "jincai"，SaaS 场景下需在创建时设置对应租户编码。
     */
    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId = "jincai";

    /** 创建人（由 Spring Data JPA Auditing 自动填充当前登录用户名）。 */
    @CreatedBy
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /** 创建时间（由 Spring Data JPA Auditing 自动填充，不允许更新）。 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 最后修改时间（由 Spring Data JPA Auditing 自动维护）。 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 最后修改人（由 Spring Data JPA Auditing 自动填充当前登录用户名）。 */
    @LastModifiedBy
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 软删除标志。
     * <ul>
     *   <li>{@code false}（默认）：记录有效。</li>
     *   <li>{@code true}：记录已逻辑删除，所有查询应包含 {@code WHERE deleted = false} 条件。</li>
     * </ul>
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 乐观锁版本号。
     *
     * <p>JPA 在每次 UPDATE 时自动将 version +1，并在 WHERE 子句中追加版本条件：
     * {@code WHERE id = ? AND version = ?}。若影响行数为 0，
     * 则抛出 {@link jakarta.persistence.OptimisticLockException}。
     *
     * <p>调用方（Service 层）应在捕获该异常后重新读取最新数据后再做业务判断或重试，
     * 而不是静默忽略，以防止数据不一致。
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    /**
     * 在实体首次持久化（INSERT）之前执行，自动生成 ULID 主键。
     * 若调用方已预设 id（如批量导入场景），则不覆盖。
     */
    @PrePersist
    protected void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UlidGenerator.generate();
        }
    }
}
