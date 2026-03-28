package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.system.entity.OrgUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TravelerRepository extends JpaRepository<Traveler, String>, JpaSpecificationExecutor<Traveler> {

    List<Traveler> findByDeletedFalse();

    List<Traveler> findByCustomerIdAndDeletedFalse(String customerId);

    // 基于部门ID的关联查询（用于数据权限过滤）
    @Query("SELECT t FROM Traveler t " +
           "JOIN Customer c ON t.customerId = c.id " +
           "JOIN OrgUser u ON c.ownerUserId = u.id " +
           "WHERE (:departmentIds IS NULL OR u.departmentId IN :departmentIds) " +
           "AND (:keyword IS NULL OR LOWER(t.name) LIKE %:keyword%) " +
           "AND (:customerId IS NULL OR t.customerId = :customerId) " +
           "AND t.deleted = false")
    Page<Traveler> findByDepartmentIdsWithKeyword(
        @Param("departmentIds") Set<String> departmentIds,
        @Param("keyword") String keyword,
        @Param("customerId") String customerId,
        Pageable pageable
    );

    // 基于用户ID的查询（用于SELF权限）
    @Query("SELECT t FROM Traveler t " +
           "JOIN Customer c ON t.customerId = c.id " +
           "JOIN OrgUser u ON c.ownerUserId = u.id " +
           "WHERE u.id = :userId " +
           "AND (:keyword IS NULL OR LOWER(t.name) LIKE %:keyword%) " +
           "AND (:customerId IS NULL OR t.customerId = :customerId) " +
           "AND t.deleted = false")
    Page<Traveler> findByUserIdWithKeyword(
        @Param("userId") String userId,
        @Param("keyword") String keyword,
        @Param("customerId") String customerId,
        Pageable pageable
    );

    // 查询所有数据（用于ALL权限）
    @Query("SELECT t FROM Traveler t " +
           "JOIN Customer c ON t.customerId = c.id " +
           "WHERE (:keyword IS NULL OR LOWER(t.name) LIKE %:keyword%) " +
           "AND (:customerId IS NULL OR t.customerId = :customerId) " +
           "AND t.deleted = false")
    Page<Traveler> findAllWithKeyword(
        @Param("keyword") String keyword,
        @Param("customerId") String customerId,
        Pageable pageable
    );
}
