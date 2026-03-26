package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.entity.FinanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinanceReviewRepository extends JpaRepository<FinanceReview, String> {

    Optional<FinanceReview> findByTargetTypeAndTargetIdAndDeletedFalse(String targetType, String targetId);
}

