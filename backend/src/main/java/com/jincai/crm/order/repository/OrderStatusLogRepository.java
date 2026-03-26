package com.jincai.crm.order.repository;

import com.jincai.crm.order.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, String> {

    List<OrderStatusLog> findByOrderIdAndDeletedFalseOrderByCreatedAtAsc(String orderId);
}

