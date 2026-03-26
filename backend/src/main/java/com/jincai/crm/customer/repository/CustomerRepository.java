package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String>, JpaSpecificationExecutor<Customer> {

    List<Customer> findByDeletedFalse();

    List<Customer> findByOwnerUserIdAndDeletedFalse(String ownerUserId);

    List<Customer> findByOwnerDeptIdAndDeletedFalse(String ownerDeptId);

    List<Customer> findByOwnerDeptIdInAndDeletedFalse(Collection<String> ownerDeptIds);

    boolean existsByPhoneAndDeletedFalse(String phone);

    boolean existsByPhoneAndIdNotAndDeletedFalse(String phone, String id);
}
