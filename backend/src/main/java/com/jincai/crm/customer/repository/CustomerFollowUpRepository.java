package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.entity.CustomerFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerFollowUpRepository extends JpaRepository<CustomerFollowUp, String> {

    List<CustomerFollowUp> findByCustomerIdAndDeletedFalse(Long customerId);
}

