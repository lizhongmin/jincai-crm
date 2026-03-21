package com.jincai.crm.order.repository;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.service.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelOrderRepository extends JpaRepository<TravelOrder, Long> {

    List<TravelOrder> findByDeletedFalse();

    List<TravelOrder> findBySalesUserIdAndDeletedFalse(Long salesUserId);

    List<TravelOrder> findBySalesDeptIdAndDeletedFalse(Long salesDeptId);

    List<TravelOrder> findBySalesDeptIdInAndDeletedFalse(Collection<Long> salesDeptIds);

    Optional<TravelOrder> findByOrderNoAndDeletedFalse(String orderNo);
}

