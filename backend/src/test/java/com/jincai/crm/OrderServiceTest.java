package com.jincai.crm;

import com.jincai.crm.audit.service.AuditLogService;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScope;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.common.I18nService;
import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.repository.CustomerRepository;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.repository.TravelerRepository;
import com.jincai.crm.order.repository.OrderPriceItemRepository;
import com.jincai.crm.order.dto.OrderPriceSelectionRequest;
import com.jincai.crm.order.dto.OrderRequest;
import com.jincai.crm.order.entity.ContractStatus;
import com.jincai.crm.order.entity.InventoryStatus;
import com.jincai.crm.order.service.OrderService;
import com.jincai.crm.order.entity.OrderStatus;
import com.jincai.crm.order.entity.OrderPaymentPolicy;
import com.jincai.crm.order.entity.PaymentStatus;
import com.jincai.crm.order.repository.OrderStatusLogRepository;
import com.jincai.crm.order.repository.OrderTravelerSnapshotRepository;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.order.repository.TravelOrderRepository;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.repository.DeparturePriceRepository;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.workflow.service.WorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderServiceTest {

    @Test
    void shouldSubmitDraftOrder() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        OrderStatusLogRepository logRepository = Mockito.mock(OrderStatusLogRepository.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);

        TravelOrder order = new TravelOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.DRAFT);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(Mockito.any(TravelOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderService orderService = new OrderService(
            orderRepository,
            logRepository,
            workflowService,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            Mockito.mock(CustomerRepository.class),
            Mockito.mock(TravelerRepository.class),
            Mockito.mock(RouteProductRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DeparturePriceRepository.class),
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        TravelOrder submitted = orderService.submit(1L);

        Assertions.assertEquals(OrderStatus.PENDING_APPROVAL, submitted.getStatus());
        Mockito.verify(workflowService).startWorkflow(Mockito.any(TravelOrder.class));
    }

    @Test
    void shouldRejectEditingApprovedOrder() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        OrderStatusLogRepository logRepository = Mockito.mock(OrderStatusLogRepository.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        AuditLogService auditLogService = Mockito.mock(AuditLogService.class);

        TravelOrder order = new TravelOrder();
        order.setId(2L);
        order.setStatus(OrderStatus.APPROVED);

        Mockito.when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        OrderService orderService = new OrderService(
            orderRepository,
            logRepository,
            workflowService,
            Mockito.mock(DataScopeResolver.class),
            auditLogService,
            Mockito.mock(CustomerRepository.class),
            Mockito.mock(TravelerRepository.class),
            Mockito.mock(RouteProductRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DeparturePriceRepository.class),
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        OrderRequest request = new OrderRequest(
            null,
            1L,
            1L,
            1L,
            "GROUP",
            "DOMESTIC",
            2,
            new BigDecimal("1000.00"),
            "CNY",
            List.of()
        );

        Assertions.assertThrows(BusinessException.class,
            () -> orderService.update(2L, request, Mockito.mock(HttpServletRequest.class)));
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(TravelOrder.class));
        Mockito.verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldRejectDeletingApprovedOrder() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        OrderStatusLogRepository logRepository = Mockito.mock(OrderStatusLogRepository.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);

        TravelOrder order = new TravelOrder();
        order.setId(3L);
        order.setStatus(OrderStatus.APPROVED);

        Mockito.when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        OrderService orderService = new OrderService(
            orderRepository,
            logRepository,
            workflowService,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            Mockito.mock(CustomerRepository.class),
            Mockito.mock(TravelerRepository.class),
            Mockito.mock(RouteProductRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DeparturePriceRepository.class),
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        Assertions.assertThrows(BusinessException.class, () -> orderService.delete(3L));
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(TravelOrder.class));
    }

    @Test
    void shouldCalculateTotalAmountFromDeparturePrices() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        OrderStatusLogRepository logRepository = Mockito.mock(OrderStatusLogRepository.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);
        OrderTravelerSnapshotRepository orderTravelerSnapshotRepository = Mockito.mock(OrderTravelerSnapshotRepository.class);
        OrderPriceItemRepository orderPriceItemRepository = Mockito.mock(OrderPriceItemRepository.class);

        Customer customer = new Customer();
        customer.setId(10L);

        Traveler traveler1 = new Traveler();
        traveler1.setId(101L);
        traveler1.setCustomerId(10L);
        traveler1.setName("Alice");

        Traveler traveler2 = new Traveler();
        traveler2.setId(102L);
        traveler2.setCustomerId(10L);
        traveler2.setName("Bob");

        RouteProduct route = new RouteProduct();
        route.setId(201L);
        route.setCategory("DOMESTIC");

        Departure departure = new Departure();
        departure.setId(301L);
        departure.setRouteId(201L);
        departure.setStock(20);
        departure.setStartDate(LocalDate.of(2026, 4, 1));
        departure.setEndDate(LocalDate.of(2026, 4, 5));

        DeparturePrice adultPrice = new DeparturePrice();
        adultPrice.setId(401L);
        adultPrice.setDepartureId(301L);
        adultPrice.setPriceType("ADULT");
        adultPrice.setPrice(new BigDecimal("1999.00"));

        DeparturePrice roomDiff = new DeparturePrice();
        roomDiff.setId(402L);
        roomDiff.setDepartureId(301L);
        roomDiff.setPriceType("SINGLE_ROOM");
        roomDiff.setPrice(new BigDecimal("600.00"));

        Mockito.when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        Mockito.when(travelerRepository.findById(101L)).thenReturn(Optional.of(traveler1));
        Mockito.when(travelerRepository.findById(102L)).thenReturn(Optional.of(traveler2));
        Mockito.when(routeRepository.findById(201L)).thenReturn(Optional.of(route));
        Mockito.when(departureRepository.findById(301L)).thenReturn(Optional.of(departure));
        Mockito.when(priceRepository.findByDepartureIdAndDeletedFalse(301L)).thenReturn(List.of(adultPrice, roomDiff));
        Mockito.when(priceRepository.findById(401L)).thenReturn(Optional.of(adultPrice));
        Mockito.when(priceRepository.findById(402L)).thenReturn(Optional.of(roomDiff));
        Mockito.when(orderRepository.save(Mockito.any(TravelOrder.class))).thenAnswer(invocation -> {
            TravelOrder saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        OrderService orderService = new OrderService(
            orderRepository,
            logRepository,
            workflowService,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            customerRepository,
            travelerRepository,
            routeRepository,
            departureRepository,
            priceRepository,
            orderTravelerSnapshotRepository,
            orderPriceItemRepository,
            Mockito.mock(I18nService.class)
        );

        OrderRequest request = new OrderRequest(
            null,
            10L,
            201L,
            301L,
            "GROUP",
            "DOMESTIC",
            null,
            null,
            "CNY",
            List.of(
                new OrderPriceSelectionRequest(101L, 401L, null),
                new OrderPriceSelectionRequest(102L, 401L, null),
                new OrderPriceSelectionRequest(102L, 402L, null)
            )
        );

        TravelOrder created = orderService.create(request, salesLogin());

        Assertions.assertEquals(2, created.getTravelerCount());
        Assertions.assertEquals(new BigDecimal("4598.00"), created.getTotalAmount());
        Mockito.verify(orderTravelerSnapshotRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(orderPriceItemRepository, Mockito.times(3)).save(Mockito.any());
    }

    @Test
    void shouldRejectTravelerOutsideCustomerWhenCalculatingOrder() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);

        Customer customer = new Customer();
        customer.setId(10L);

        Traveler traveler = new Traveler();
        traveler.setId(101L);
        traveler.setCustomerId(99L);

        RouteProduct route = new RouteProduct();
        route.setId(201L);
        route.setCategory("DOMESTIC");

        Departure departure = new Departure();
        departure.setId(301L);
        departure.setRouteId(201L);
        departure.setStock(20);

        DeparturePrice price = new DeparturePrice();
        price.setId(401L);
        price.setDepartureId(301L);
        price.setPriceType("ADULT");
        price.setPrice(new BigDecimal("1999.00"));

        Mockito.when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        Mockito.when(travelerRepository.findById(101L)).thenReturn(Optional.of(traveler));
        Mockito.when(routeRepository.findById(201L)).thenReturn(Optional.of(route));
        Mockito.when(departureRepository.findById(301L)).thenReturn(Optional.of(departure));
        Mockito.when(priceRepository.findByDepartureIdAndDeletedFalse(301L)).thenReturn(List.of(price));
        Mockito.when(priceRepository.findById(401L)).thenReturn(Optional.of(price));

        OrderService orderService = new OrderService(
            orderRepository,
            Mockito.mock(OrderStatusLogRepository.class),
            Mockito.mock(WorkflowService.class),
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            customerRepository,
            travelerRepository,
            routeRepository,
            departureRepository,
            priceRepository,
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        OrderRequest request = new OrderRequest(
            null,
            10L,
            201L,
            301L,
            "GROUP",
            "DOMESTIC",
            null,
            null,
            "CNY",
            List.of(new OrderPriceSelectionRequest(101L, 401L, null))
        );

        Assertions.assertThrows(BusinessException.class, () -> orderService.create(request, salesLogin()));
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(TravelOrder.class));
    }

    @Test
    void shouldRejectNonCnyCurrencyWhenCreatingOrder() {
        OrderService orderService = new OrderService(
            Mockito.mock(TravelOrderRepository.class),
            Mockito.mock(OrderStatusLogRepository.class),
            Mockito.mock(WorkflowService.class),
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            Mockito.mock(CustomerRepository.class),
            Mockito.mock(TravelerRepository.class),
            Mockito.mock(RouteProductRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DeparturePriceRepository.class),
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        OrderRequest request = new OrderRequest(
            null,
            1L,
            1L,
            1L,
            "GROUP",
            "DOMESTIC",
            2,
            new BigDecimal("1000.00"),
            "USD",
            List.of()
        );

        Assertions.assertThrows(BusinessException.class, () -> orderService.create(request, salesLogin()));
    }

    @Test
    void shouldRejectNonCnyDeparturePriceWhenCalculatingOrder() {
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);

        Customer customer = new Customer();
        customer.setId(10L);
        Traveler traveler = new Traveler();
        traveler.setId(101L);
        traveler.setCustomerId(10L);

        RouteProduct route = new RouteProduct();
        route.setId(201L);
        route.setCategory("DOMESTIC");
        Departure departure = new Departure();
        departure.setId(301L);
        departure.setRouteId(201L);
        departure.setStock(10);

        DeparturePrice price = new DeparturePrice();
        price.setId(401L);
        price.setDepartureId(301L);
        price.setPriceType("ADULT");
        price.setPrice(new BigDecimal("1999.00"));
        price.setCurrency("USD");

        Mockito.when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        Mockito.when(travelerRepository.findById(101L)).thenReturn(Optional.of(traveler));
        Mockito.when(routeRepository.findById(201L)).thenReturn(Optional.of(route));
        Mockito.when(departureRepository.findById(301L)).thenReturn(Optional.of(departure));
        Mockito.when(priceRepository.findByDepartureIdAndDeletedFalse(301L)).thenReturn(List.of(price));
        Mockito.when(priceRepository.findById(401L)).thenReturn(Optional.of(price));

        OrderService orderService = new OrderService(
            Mockito.mock(TravelOrderRepository.class),
            Mockito.mock(OrderStatusLogRepository.class),
            Mockito.mock(WorkflowService.class),
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            customerRepository,
            travelerRepository,
            routeRepository,
            departureRepository,
            priceRepository,
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        OrderRequest request = new OrderRequest(
            null,
            10L,
            201L,
            301L,
            "GROUP",
            "DOMESTIC",
            null,
            null,
            "CNY",
            List.of(new OrderPriceSelectionRequest(101L, 401L, 1))
        );

        Assertions.assertThrows(BusinessException.class, () -> orderService.create(request, salesLogin()));
    }

    @Test
    void shouldAutoSelectPriceTypeByTravelerAge() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);
        OrderTravelerSnapshotRepository orderTravelerSnapshotRepository = Mockito.mock(OrderTravelerSnapshotRepository.class);
        OrderPriceItemRepository orderPriceItemRepository = Mockito.mock(OrderPriceItemRepository.class);

        Customer customer = new Customer();
        customer.setId(10L);

        Traveler childTraveler = new Traveler();
        childTraveler.setId(101L);
        childTraveler.setCustomerId(10L);
        childTraveler.setName("Child");
        childTraveler.setBirthday(LocalDate.of(2020, 5, 1));

        RouteProduct route = new RouteProduct();
        route.setId(201L);
        route.setCategory("DOMESTIC");

        Departure departure = new Departure();
        departure.setId(301L);
        departure.setRouteId(201L);
        departure.setStock(20);
        departure.setStartDate(LocalDate.of(2026, 4, 1));

        DeparturePrice adultPrice = new DeparturePrice();
        adultPrice.setId(401L);
        adultPrice.setDepartureId(301L);
        adultPrice.setPriceType("ADULT");
        adultPrice.setPrice(new BigDecimal("1999.00"));

        DeparturePrice childPrice = new DeparturePrice();
        childPrice.setId(402L);
        childPrice.setDepartureId(301L);
        childPrice.setPriceType("CHILD");
        childPrice.setPrice(new BigDecimal("1599.00"));

        Mockito.when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        Mockito.when(travelerRepository.findById(101L)).thenReturn(Optional.of(childTraveler));
        Mockito.when(routeRepository.findById(201L)).thenReturn(Optional.of(route));
        Mockito.when(departureRepository.findById(301L)).thenReturn(Optional.of(departure));
        Mockito.when(priceRepository.findByDepartureIdAndDeletedFalse(301L)).thenReturn(List.of(adultPrice, childPrice));
        Mockito.when(orderRepository.save(Mockito.any(TravelOrder.class))).thenAnswer(invocation -> {
            TravelOrder saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        OrderService orderService = new OrderService(
            orderRepository,
            Mockito.mock(OrderStatusLogRepository.class),
            Mockito.mock(WorkflowService.class),
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            customerRepository,
            travelerRepository,
            routeRepository,
            departureRepository,
            priceRepository,
            orderTravelerSnapshotRepository,
            orderPriceItemRepository,
            Mockito.mock(I18nService.class)
        );

        OrderRequest request = new OrderRequest(
            null,
            10L,
            201L,
            301L,
            "GROUP",
            "DOMESTIC",
            null,
            null,
            "CNY",
            List.of(new OrderPriceSelectionRequest(101L, null, 1))
        );

        TravelOrder created = orderService.create(request, salesLogin());

        Assertions.assertEquals(1, created.getTravelerCount());
        Assertions.assertEquals(new BigDecimal("1599.00"), created.getTotalAmount());
        Mockito.verify(orderPriceItemRepository).save(Mockito.argThat(item ->
            "CHILD".equals(item.getPriceType()) && new BigDecimal("1599.00").equals(item.getUnitPrice())
        ));
    }

    @Test
    void shouldAutoCancelOverdueApprovedOrders() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        OrderStatusLogRepository logRepository = Mockito.mock(OrderStatusLogRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);

        TravelOrder overdueOrder = new TravelOrder();
        overdueOrder.setId(1L);
        overdueOrder.setStatus(OrderStatus.APPROVED);
        overdueOrder.setApprovedAt(LocalDateTime.now().minusHours(30));
        overdueOrder.setAutoCancelHours(24);
        overdueOrder.setContractRequired(true);
        overdueOrder.setContractStatus(ContractStatus.PENDING_SIGN);
        overdueOrder.setPaymentPolicy(OrderPaymentPolicy.DEPOSIT_BALANCE);
        overdueOrder.setPaymentStatus(PaymentStatus.UNPAID);
        overdueOrder.setInventoryStatus(InventoryStatus.LOCKED);
        overdueOrder.setDepartureId(301L);
        overdueOrder.setTravelerCount(2);

        TravelOrder normalOrder = new TravelOrder();
        normalOrder.setId(2L);
        normalOrder.setStatus(OrderStatus.APPROVED);
        normalOrder.setApprovedAt(LocalDateTime.now().minusHours(2));
        normalOrder.setAutoCancelHours(24);
        normalOrder.setContractRequired(true);
        normalOrder.setContractStatus(ContractStatus.PENDING_SIGN);
        normalOrder.setPaymentPolicy(OrderPaymentPolicy.DEPOSIT_BALANCE);
        normalOrder.setPaymentStatus(PaymentStatus.UNPAID);
        normalOrder.setInventoryStatus(InventoryStatus.LOCKED);
        normalOrder.setDepartureId(301L);
        normalOrder.setTravelerCount(1);

        Departure departure = new Departure();
        departure.setId(301L);
        departure.setStock(8);

        Mockito.when(orderRepository.findByStatusInAndDeletedFalse(Mockito.any())).thenReturn(List.of(overdueOrder, normalOrder));
        Mockito.when(orderRepository.save(Mockito.any(TravelOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(departureRepository.findById(301L)).thenReturn(Optional.of(departure));
        Mockito.when(departureRepository.save(Mockito.any(Departure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderService orderService = new OrderService(
            orderRepository,
            logRepository,
            workflowService,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AuditLogService.class),
            Mockito.mock(CustomerRepository.class),
            Mockito.mock(TravelerRepository.class),
            Mockito.mock(RouteProductRepository.class),
            departureRepository,
            Mockito.mock(DeparturePriceRepository.class),
            Mockito.mock(OrderTravelerSnapshotRepository.class),
            Mockito.mock(OrderPriceItemRepository.class),
            Mockito.mock(I18nService.class)
        );

        int canceled = orderService.autoCancelOverdueOrders();

        Assertions.assertEquals(1, canceled);
        Assertions.assertEquals(OrderStatus.CANCELED, overdueOrder.getStatus());
        Assertions.assertEquals(OrderStatus.APPROVED, normalOrder.getStatus());
        Assertions.assertEquals(10, departure.getStock());
        Mockito.verify(logRepository, Mockito.times(1)).save(Mockito.any());
    }

    private LoginUser salesLogin() {
        return new LoginUser(88L, 9L, DataScope.SELF, "sales", "pwd", true, List.of("SALES"));
    }
}

