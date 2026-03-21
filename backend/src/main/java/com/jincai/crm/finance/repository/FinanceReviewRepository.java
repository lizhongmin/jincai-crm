package com.jincai.crm.finance.repository;

import com.jincai.crm.finance.controller.*;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.service.*;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceReviewRepository extends JpaRepository<FinanceReview, Long> {

    Optional<FinanceReview> findByTargetTypeAndTargetIdAndDeletedFalse(String targetType, Long targetId);
}

