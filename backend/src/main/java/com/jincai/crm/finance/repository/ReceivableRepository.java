package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.entity.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceivableRepository extends JpaRepository<Receivable, String> {

    List<Receivable> findByOrderIdAndDeletedFalse(String orderId);

    List<Receivable> findByDeletedFalse();
}

