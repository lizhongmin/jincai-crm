package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.entity.Payable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayableRepository extends JpaRepository<Payable, String> {

    List<Payable> findByOrderIdAndDeletedFalse(String orderId);

    List<Payable> findByDeletedFalse();
}

