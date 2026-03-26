package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.service.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    List<Customer> findByDeletedFalse();

    List<Customer> findByOwnerUserIdAndDeletedFalse(Long ownerUserId);

    List<Customer> findByOwnerDeptIdAndDeletedFalse(Long ownerDeptId);

    List<Customer> findByOwnerDeptIdInAndDeletedFalse(Collection<Long> ownerDeptIds);

    boolean existsByPhoneAndDeletedFalse(String phone);

    boolean existsByPhoneAndIdNotAndDeletedFalse(String phone, Long id);
}
