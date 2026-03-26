package com.jincai.crm.order.repository;

import com.jincai.crm.order.entity.OrderPriceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderPriceItemRepository extends JpaRepository<OrderPriceItem, String> {

    List<OrderPriceItem> findByOrderIdAndDeletedFalse(String orderId);
}
