package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, String> {

    List<Receipt> findByReceivableIdAndDeletedFalse(String receivableId);

    List<Receipt> findByDeletedFalse();
}

