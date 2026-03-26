package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {

    List<Refund> findByOrderIdAndDeletedFalse(String orderId);

    List<Refund> findByDeletedFalse();
}

