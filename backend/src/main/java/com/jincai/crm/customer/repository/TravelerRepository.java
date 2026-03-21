package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelerRepository extends JpaRepository<Traveler, Long> {

    List<Traveler> findByDeletedFalse();

    List<Traveler> findByCustomerIdAndDeletedFalse(Long customerId);
}

