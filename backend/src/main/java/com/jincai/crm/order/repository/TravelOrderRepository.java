package com.jincai.crm.order.repository;

import com.jincai.crm.order.entity.OrderStatus;
import com.jincai.crm.order.entity.TravelOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TravelOrderRepository extends JpaRepository<TravelOrder, String>, JpaSpecificationExecutor<TravelOrder> {

    List<TravelOrder> findByDeletedFalse();

    List<TravelOrder> findBySalesUserIdAndDeletedFalse(String salesUserId);

    List<TravelOrder> findBySalesDeptIdAndDeletedFalse(String salesDeptId);

    List<TravelOrder> findBySalesDeptIdInAndDeletedFalse(Collection<String> salesDeptIds);

    List<TravelOrder> findByStatusInAndDeletedFalse(Collection<OrderStatus> statuses);

    Optional<TravelOrder> findByOrderNoAndDeletedFalse(String orderNo);
}
