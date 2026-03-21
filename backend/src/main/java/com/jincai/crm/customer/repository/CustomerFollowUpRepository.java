package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerFollowUpRepository extends JpaRepository<CustomerFollowUp, Long> {

    List<CustomerFollowUp> findByCustomerIdAndDeletedFalse(Long customerId);
}

