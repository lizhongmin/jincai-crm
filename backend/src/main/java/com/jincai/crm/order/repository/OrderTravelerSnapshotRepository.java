package com.jincai.crm.order.repository;

import com.jincai.crm.order.entity.OrderTravelerSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderTravelerSnapshotRepository extends JpaRepository<OrderTravelerSnapshot, String> {

    List<OrderTravelerSnapshot> findByOrderIdAndDeletedFalse(String orderId);
}
