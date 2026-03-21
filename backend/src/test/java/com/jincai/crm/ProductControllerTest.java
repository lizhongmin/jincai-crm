package com.jincai.crm;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.repository.DeparturePriceRepository;
import com.jincai.crm.product.dto.DeparturePriceRequest;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.dto.DepartureRequest;
import com.jincai.crm.product.controller.ProductController;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.product.service.ProductService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProductControllerTest {

    @Test
    void shouldRejectDepartureWhenEndDateEarlierThanStartDate() {
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);

        RouteProduct route = new RouteProduct();
        route.setId(1L);
        Mockito.when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        ProductController controller = new ProductController(new ProductService(routeRepository, departureRepository, priceRepository));

        DepartureRequest request = new DepartureRequest(
            1L,
            "D20260401",
            "2026-04-05",
            "2026-04-01",
            20,
            "2026-03-30",
            1,
            20,
            "OPEN",
            "Shanghai",
            "Gather 2 hours in advance"
        );

        Assertions.assertThrows(BusinessException.class, () -> controller.createDeparture(request));
    }

    @Test
    void shouldRejectDepartureWhenMinGroupSizeGreaterThanMax() {
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);

        RouteProduct route = new RouteProduct();
        route.setId(1L);
        Mockito.when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        ProductController controller = new ProductController(new ProductService(routeRepository, departureRepository, priceRepository));

        DepartureRequest request = new DepartureRequest(
            1L,
            "D20260501",
            "2026-05-01",
            "2026-05-06",
            10,
            "2026-04-28",
            12,
            10,
            "OPEN",
            "Beijing",
            "Bring valid id document"
        );

        Assertions.assertThrows(BusinessException.class, () -> controller.createDeparture(request));
    }

    @Test
    void shouldRejectNonCnyPrice() {
        RouteProductRepository routeRepository = Mockito.mock(RouteProductRepository.class);
        DepartureRepository departureRepository = Mockito.mock(DepartureRepository.class);
        DeparturePriceRepository priceRepository = Mockito.mock(DeparturePriceRepository.class);

        Departure departure = new Departure();
        departure.setId(9L);
        Mockito.when(departureRepository.findById(9L)).thenReturn(Optional.of(departure));

        ProductController controller = new ProductController(new ProductService(routeRepository, departureRepository, priceRepository));

        DeparturePriceRequest request = new DeparturePriceRequest(
            "ADULT",
            "Adult Price",
            new BigDecimal("1999.00"),
            "USD",
            "Foreign currency not allowed"
        );

        Assertions.assertThrows(BusinessException.class, () -> controller.addPrice(9L, request));
    }
}


