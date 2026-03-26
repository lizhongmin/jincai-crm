package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByPayableIdAndDeletedFalse(String payableId);

    List<Payment> findByDeletedFalse();
}

