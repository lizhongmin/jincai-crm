package com.jincai.crm.product.service;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.repository.*;

import com.jincai.crm.common.BusinessException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final String CNY = "CNY";
    private static final Set<String> ALLOWED_PRICE_TYPES = Set.of("ADULT", "CHILD", "SINGLE_ROOM", "EXTRA");
    private static final Set<String> ALLOWED_DEPARTURE_STATUSES = Set.of("OPEN", "CLOSED", "FULL");

    private final RouteProductRepository routeRepository;
    private final DepartureRepository departureRepository;
    private final DeparturePriceRepository priceRepository;

    public ProductService(RouteProductRepository routeRepository, DepartureRepository departureRepository,
                          DeparturePriceRepository priceRepository) {
        this.routeRepository = routeRepository;
        this.departureRepository = departureRepository;
        this.priceRepository = priceRepository;
    }

    public List<RouteProduct> routes() {
        return routeRepository.findByDeletedFalse();
    }

    @Transactional
    public RouteProduct createRoute(@Valid RouteRequest request) {
        RouteProduct route = new RouteProduct();
        applyRoute(route, request);
        return routeRepository.save(route);
    }

    @Transactional
    public RouteProduct updateRoute(Long id, @Valid RouteRequest request) {
        RouteProduct route = routeRepository.findById(id).orElseThrow(() -> new BusinessException("Route not found"));
        applyRoute(route, request);
        return routeRepository.save(route);
    }

    @Transactional
    public void deleteRoute(Long id) {
        RouteProduct route = routeRepository.findById(id).orElseThrow(() -> new BusinessException("Route not found"));
        route.setDeleted(true);
        routeRepository.save(route);
    }

    public List<Departure> departures(Long routeId) {
        if (routeId != null) {
            return departureRepository.findByRouteIdAndDeletedFalse(routeId);
        }
        return departureRepository.findByDeletedFalse();
    }

    @Transactional
    public Departure createDeparture(@Valid DepartureRequest request) {
        routeRepository.findById(request.routeId()).orElseThrow(() -> new BusinessException("Route not found"));
        Departure departure = new Departure();
        applyDeparture(departure, request);
        return departureRepository.save(departure);
    }

    @Transactional
    public Departure updateDeparture(Long id, @Valid DepartureRequest request) {
        routeRepository.findById(request.routeId()).orElseThrow(() -> new BusinessException("Route not found"));
        Departure departure = departureRepository.findById(id).orElseThrow(() -> new BusinessException("Departure not found"));
        applyDeparture(departure, request);
        return departureRepository.save(departure);
    }

    @Transactional
    public void deleteDeparture(Long id) {
        Departure departure = departureRepository.findById(id).orElseThrow(() -> new BusinessException("Departure not found"));
        departure.setDeleted(true);
        departureRepository.save(departure);
    }

    public List<DeparturePrice> prices(Long departureId) {
        return priceRepository.findByDepartureIdAndDeletedFalse(departureId);
    }

    @Transactional
    public DeparturePrice addPrice(Long departureId, @Valid DeparturePriceRequest request) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("Departure not found"));
        DeparturePrice price = new DeparturePrice();
        price.setDepartureId(departureId);
        applyPrice(price, request);
        return priceRepository.save(price);
    }

    @Transactional
    public DeparturePrice updatePrice(Long departureId, Long priceId, @Valid DeparturePriceRequest request) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("Departure not found"));
        DeparturePrice price = priceRepository.findById(priceId).orElseThrow(() -> new BusinessException("Price not found"));
        applyPrice(price, request);
        return priceRepository.save(price);
    }

    @Transactional
    public void deletePrice(Long departureId, Long priceId) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("Departure not found"));
        DeparturePrice price = priceRepository.findById(priceId).orElseThrow(() -> new BusinessException("Price not found"));
        price.setDeleted(true);
        priceRepository.save(price);
    }

    private void applyRoute(RouteProduct route, RouteRequest request) {
        route.setCode(request.code());
        route.setName(request.name());
        route.setCategory(request.category());
        route.setDepartureCity(request.departureCity());
        route.setDestinationCity(request.destinationCity());
        route.setDurationDays(request.durationDays());
        route.setDurationNights(request.durationNights());
        route.setTransportation(request.transportation());
        route.setHotelStandard(request.hotelStandard());
        route.setHighlights(request.highlights());
        route.setFeeIncludes(request.feeIncludes());
        route.setFeeExcludes(request.feeExcludes());
        route.setBookingNotice(request.bookingNotice());
        route.setDescription(request.description());
    }

    private void applyDeparture(Departure departure, DepartureRequest request) {
        LocalDate startDate = LocalDate.parse(request.startDate(), DateTimeFormatter.ISO_DATE);
        LocalDate endDate = LocalDate.parse(request.endDate(), DateTimeFormatter.ISO_DATE);
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date cannot be earlier than start date");
        }

        LocalDate registrationDeadline = parseDate(request.registrationDeadline());
        if (registrationDeadline != null && registrationDeadline.isAfter(startDate)) {
            throw new BusinessException("Registration deadline cannot be later than start date");
        }

        Integer minGroupSize = request.minGroupSize();
        Integer maxGroupSize = request.maxGroupSize();
        if (minGroupSize != null && maxGroupSize != null && minGroupSize > maxGroupSize) {
            throw new BusinessException("Min group size cannot be greater than max group size");
        }
        if (maxGroupSize != null && request.stock() != null && maxGroupSize > request.stock()) {
            throw new BusinessException("Max group size cannot be greater than stock");
        }

        String status = request.status() == null || request.status().isBlank()
            ? "OPEN"
            : request.status().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_DEPARTURE_STATUSES.contains(status)) {
            throw new BusinessException("Invalid departure status");
        }

        departure.setRouteId(request.routeId());
        departure.setCode(request.code());
        departure.setStartDate(startDate);
        departure.setEndDate(endDate);
        departure.setStock(request.stock());
        departure.setRegistrationDeadline(registrationDeadline);
        departure.setMinGroupSize(minGroupSize);
        departure.setMaxGroupSize(maxGroupSize);
        departure.setStatus(status);
        departure.setGatheringPlace(request.gatheringPlace());
        departure.setDepartureNotice(request.departureNotice());
    }

    private void applyPrice(DeparturePrice price, DeparturePriceRequest request) {
        String normalizedType = request.priceType().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_PRICE_TYPES.contains(normalizedType)) {
            throw new BusinessException("Invalid price type");
        }
        if (request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be greater than 0");
        }
        if (request.currency() != null && !request.currency().isBlank()
            && !CNY.equalsIgnoreCase(request.currency().trim())) {
            throw new BusinessException("Only CNY is supported in current version");
        }

        price.setPriceType(normalizedType);
        price.setPriceLabel(request.priceLabel());
        price.setPrice(request.price().setScale(2, java.math.RoundingMode.HALF_UP));
        price.setCurrency(CNY);
        price.setDescription(request.description());
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
    }
}
