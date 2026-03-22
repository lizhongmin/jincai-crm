package com.jincai.crm.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.order.auto-cancel.enabled", havingValue = "true")
public class OrderAutoCancelScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelScheduler.class);

    private final OrderService orderService;

    public OrderAutoCancelScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "${app.order.auto-cancel.cron:0 */10 * * * ?}")
    public void autoCancelOverdueOrders() {
        int canceledCount = orderService.autoCancelOverdueOrders();
        if (canceledCount > 0) {
            log.info("Auto canceled overdue orders count={}", canceledCount);
        }
    }
}
