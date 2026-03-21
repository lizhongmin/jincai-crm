package com.jincai.crm.order.repository;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPriceItemRepository extends JpaRepository<OrderPriceItem, Long> {

    List<OrderPriceItem> findByOrderIdAndDeletedFalse(Long orderId);
}
