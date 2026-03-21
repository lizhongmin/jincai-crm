package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.controller.*;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPayableIdAndDeletedFalse(Long payableId);

    List<Payment> findByDeletedFalse();
}

