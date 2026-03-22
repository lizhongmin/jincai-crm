package com.jincai.crm.product.service;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.order.entity.DepositRuleType;
import com.jincai.crm.order.entity.OrderLockPolicy;
import com.jincai.crm.order.entity.OrderPaymentPolicy;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final String CNY = "CNY";
    private static final String ROUTE_CODE_PREFIX = "LX";
    private static final String DEPARTURE_CODE_PREFIX = "TQ";
    private static final Set<String> STANDARD_PRICE_TYPES = Set.of("ADULT", "CHILD", "INFANT", "SINGLE_ROOM", "EXTRA");
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
        RouteProduct route = routeRepository.findById(id).orElseThrow(() -> new BusinessException("error.route.notFound"));
        applyRoute(route, request);
        return routeRepository.save(route);
    }

    @Transactional
    public void deleteRoute(Long id) {
        RouteProduct route = routeRepository.findById(id).orElseThrow(() -> new BusinessException("error.route.notFound"));
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
        routeRepository.findById(request.routeId()).orElseThrow(() -> new BusinessException("error.route.notFound"));
        Departure departure = new Departure();
        applyDeparture(departure, request);
        return departureRepository.save(departure);
    }

    @Transactional
    public Departure updateDeparture(Long id, @Valid DepartureRequest request) {
        routeRepository.findById(request.routeId()).orElseThrow(() -> new BusinessException("error.route.notFound"));
        Departure departure = departureRepository.findById(id).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        applyDeparture(departure, request);
        return departureRepository.save(departure);
    }

    @Transactional
    public void deleteDeparture(Long id) {
        Departure departure = departureRepository.findById(id).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        departure.setDeleted(true);
        departureRepository.save(departure);
    }

    public List<DeparturePrice> prices(Long departureId) {
        return priceRepository.findByDepartureIdAndDeletedFalse(departureId);
    }

    public OrderPolicyView routePolicy(Long routeId) {
        RouteProduct route = routeRepository.findById(routeId).orElseThrow(() -> new BusinessException("error.route.notFound"));
        normalizeRoutePolicyDefaults(route);
        return toOrderPolicyView(route);
    }

    @Transactional
    public OrderPolicyView updateRoutePolicy(Long routeId, @Valid OrderPolicyRequest request) {
        RouteProduct route = routeRepository.findById(routeId).orElseThrow(() -> new BusinessException("error.route.notFound"));
        applyRoutePolicy(route, request);
        RouteProduct saved = routeRepository.save(route);
        return toOrderPolicyView(saved);
    }

    public DepartureOrderPolicyView departurePolicy(Long departureId) {
        Departure departure = departureRepository.findById(departureId)
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        RouteProduct route = routeRepository.findById(departure.getRouteId())
            .orElseThrow(() -> new BusinessException("error.route.notFound"));
        normalizeRoutePolicyDefaults(route);
        return new DepartureOrderPolicyView(
            resolveEffectivePolicy(route, departure),
            new OrderPolicyRequest(
                departure.getContractRequiredOverride(),
                departure.getLockPolicyOverride(),
                departure.getPaymentPolicyOverride(),
                departure.getDepositTypeOverride(),
                departure.getDepositValueOverride(),
                departure.getDepositDeadlineDaysOverride(),
                departure.getBalanceDeadlineDaysOverride(),
                departure.getAutoCancelHoursOverride()
            )
        );
    }

    @Transactional
    public DepartureOrderPolicyView updateDeparturePolicy(Long departureId, @Valid OrderPolicyRequest request) {
        Departure departure = departureRepository.findById(departureId)
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        RouteProduct route = routeRepository.findById(departure.getRouteId())
            .orElseThrow(() -> new BusinessException("error.route.notFound"));
        normalizeRoutePolicyDefaults(route);
        validatePolicy(request, false);

        departure.setContractRequiredOverride(request.contractRequired());
        departure.setLockPolicyOverride(request.lockPolicy());
        departure.setPaymentPolicyOverride(request.paymentPolicy());
        departure.setDepositTypeOverride(request.depositType());
        departure.setDepositValueOverride(scaleMoney(request.depositValue()));
        departure.setDepositDeadlineDaysOverride(request.depositDeadlineDays());
        departure.setBalanceDeadlineDaysOverride(request.balanceDeadlineDays());
        departure.setAutoCancelHoursOverride(request.autoCancelHours());
        Departure saved = departureRepository.save(departure);

        return new DepartureOrderPolicyView(
            resolveEffectivePolicy(route, saved),
            new OrderPolicyRequest(
                saved.getContractRequiredOverride(),
                saved.getLockPolicyOverride(),
                saved.getPaymentPolicyOverride(),
                saved.getDepositTypeOverride(),
                saved.getDepositValueOverride(),
                saved.getDepositDeadlineDaysOverride(),
                saved.getBalanceDeadlineDaysOverride(),
                saved.getAutoCancelHoursOverride()
            )
        );
    }

    @Transactional
    public DeparturePrice addPrice(Long departureId, @Valid DeparturePriceRequest request) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        DeparturePrice price = new DeparturePrice();
        price.setDepartureId(departureId);
        applyPrice(price, request);
        return priceRepository.save(price);
    }

    @Transactional
    public DeparturePrice updatePrice(Long departureId, Long priceId, @Valid DeparturePriceRequest request) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        DeparturePrice price = priceRepository.findById(priceId).orElseThrow(() -> new BusinessException("error.price.notFound"));
        applyPrice(price, request);
        return priceRepository.save(price);
    }

    @Transactional
    public void deletePrice(Long departureId, Long priceId) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        DeparturePrice price = priceRepository.findById(priceId).orElseThrow(() -> new BusinessException("error.price.notFound"));
        price.setDeleted(true);
        priceRepository.save(price);
    }

    private void applyRoute(RouteProduct route, RouteRequest request) {
        route.setCode(resolveRouteCode(route.getCode(), request.code()));
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
        normalizeRoutePolicyDefaults(route);
    }

    private void applyDeparture(Departure departure, DepartureRequest request) {
        LocalDate startDate = LocalDate.parse(request.startDate(), DateTimeFormatter.ISO_DATE);
        LocalDate endDate = LocalDate.parse(request.endDate(), DateTimeFormatter.ISO_DATE);
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("error.departure.date.invalidRange");
        }

        LocalDate registrationDeadline = parseDate(request.registrationDeadline());
        if (registrationDeadline != null && registrationDeadline.isAfter(startDate)) {
            throw new BusinessException("error.departure.registrationDeadline.invalid");
        }

        Integer minGroupSize = request.minGroupSize();
        Integer maxGroupSize = request.maxGroupSize();
        if (minGroupSize != null && maxGroupSize != null && minGroupSize > maxGroupSize) {
            throw new BusinessException("error.departure.groupSize.invalidRange");
        }
        if (maxGroupSize != null && request.stock() != null && maxGroupSize > request.stock()) {
            throw new BusinessException("error.departure.maxGroup.exceedsStock");
        }

        String status = request.status() == null || request.status().isBlank()
            ? "OPEN"
            : request.status().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_DEPARTURE_STATUSES.contains(status)) {
            throw new BusinessException("error.departure.status.invalid");
        }

        departure.setRouteId(request.routeId());
        departure.setCode(resolveDepartureCode(departure.getCode(), request.code()));
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

    private String resolveRouteCode(String existingCode, String requestCode) {
        String normalized = normalizeCode(requestCode);
        if (normalized != null) {
            return normalized;
        }
        if (existingCode != null && !existingCode.isBlank()) {
            return existingCode;
        }
        return generateUniqueCode(ROUTE_CODE_PREFIX, routeRepository::existsByCode);
    }

    private String resolveDepartureCode(String existingCode, String requestCode) {
        String normalized = normalizeCode(requestCode);
        if (normalized != null) {
            return normalized;
        }
        if (existingCode != null && !existingCode.isBlank()) {
            return existingCode;
        }
        return generateUniqueCode(DEPARTURE_CODE_PREFIX, departureRepository::existsByCode);
    }

    private String normalizeCode(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        String normalized = rawCode.trim();
        return normalized.isBlank() ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String generateUniqueCode(String prefix, Predicate<String> existsChecker) {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < 20; attempt++) {
            int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
            String code = prefix + datePart + randomPart;
            if (!existsChecker.test(code)) {
                return code;
            }
        }
        String fallback = prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + ThreadLocalRandom.current().nextInt(10, 100);
        if (!existsChecker.test(fallback)) {
            return fallback;
        }
        throw new BusinessException("common.server.error");
    }

    private void applyRoutePolicy(RouteProduct route, OrderPolicyRequest request) {
        normalizeRoutePolicyDefaults(route);
        OrderPolicyRequest normalizedRequest = new OrderPolicyRequest(
            request.contractRequired() == null ? route.getContractRequiredDefault() : request.contractRequired(),
            request.lockPolicy() == null ? route.getLockPolicyDefault() : request.lockPolicy(),
            request.paymentPolicy() == null ? route.getPaymentPolicyDefault() : request.paymentPolicy(),
            request.depositType() == null ? route.getDepositTypeDefault() : request.depositType(),
            request.depositValue() == null ? route.getDepositValueDefault() : request.depositValue(),
            request.depositDeadlineDays() == null ? route.getDepositDeadlineDaysDefault() : request.depositDeadlineDays(),
            request.balanceDeadlineDays() == null ? route.getBalanceDeadlineDaysDefault() : request.balanceDeadlineDays(),
            request.autoCancelHours() == null ? route.getAutoCancelHoursDefault() : request.autoCancelHours()
        );
        validatePolicy(normalizedRequest, true);

        route.setContractRequiredDefault(normalizedRequest.contractRequired());
        route.setLockPolicyDefault(normalizedRequest.lockPolicy());
        route.setPaymentPolicyDefault(normalizedRequest.paymentPolicy());
        route.setDepositTypeDefault(normalizedRequest.depositType());
        route.setDepositValueDefault(scaleMoney(normalizedRequest.depositValue()));
        route.setDepositDeadlineDaysDefault(normalizedRequest.depositDeadlineDays());
        route.setBalanceDeadlineDaysDefault(normalizedRequest.balanceDeadlineDays());
        route.setAutoCancelHoursDefault(normalizedRequest.autoCancelHours());
    }

    private OrderPolicyView toOrderPolicyView(RouteProduct route) {
        normalizeRoutePolicyDefaults(route);
        return new OrderPolicyView(
            route.getContractRequiredDefault(),
            route.getLockPolicyDefault(),
            route.getPaymentPolicyDefault(),
            route.getDepositTypeDefault(),
            scaleMoney(route.getDepositValueDefault()),
            route.getDepositDeadlineDaysDefault(),
            route.getBalanceDeadlineDaysDefault(),
            route.getAutoCancelHoursDefault()
        );
    }

    private OrderPolicyView resolveEffectivePolicy(RouteProduct route, Departure departure) {
        normalizeRoutePolicyDefaults(route);
        return new OrderPolicyView(
            departure.getContractRequiredOverride() != null ? departure.getContractRequiredOverride() : route.getContractRequiredDefault(),
            departure.getLockPolicyOverride() != null ? departure.getLockPolicyOverride() : route.getLockPolicyDefault(),
            departure.getPaymentPolicyOverride() != null ? departure.getPaymentPolicyOverride() : route.getPaymentPolicyDefault(),
            departure.getDepositTypeOverride() != null ? departure.getDepositTypeOverride() : route.getDepositTypeDefault(),
            scaleMoney(departure.getDepositValueOverride() != null ? departure.getDepositValueOverride() : route.getDepositValueDefault()),
            departure.getDepositDeadlineDaysOverride() != null
                ? departure.getDepositDeadlineDaysOverride() : route.getDepositDeadlineDaysDefault(),
            departure.getBalanceDeadlineDaysOverride() != null
                ? departure.getBalanceDeadlineDaysOverride() : route.getBalanceDeadlineDaysDefault(),
            departure.getAutoCancelHoursOverride() != null
                ? departure.getAutoCancelHoursOverride() : route.getAutoCancelHoursDefault()
        );
    }

    private void normalizeRoutePolicyDefaults(RouteProduct route) {
        if (route.getContractRequiredDefault() == null) {
            route.setContractRequiredDefault(false);
        }
        if (route.getLockPolicyDefault() == null) {
            route.setLockPolicyDefault(OrderLockPolicy.ON_DEPOSIT);
        }
        if (route.getPaymentPolicyDefault() == null) {
            route.setPaymentPolicyDefault(OrderPaymentPolicy.DEPOSIT_BALANCE);
        }
        if (route.getDepositTypeDefault() == null) {
            route.setDepositTypeDefault(DepositRuleType.PERCENT);
        }
        if (route.getDepositValueDefault() == null) {
            route.setDepositValueDefault(new BigDecimal("30.00"));
        }
        if (route.getDepositDeadlineDaysDefault() == null) {
            route.setDepositDeadlineDaysDefault(3);
        }
        if (route.getBalanceDeadlineDaysDefault() == null) {
            route.setBalanceDeadlineDaysDefault(7);
        }
        if (route.getAutoCancelHoursDefault() == null) {
            route.setAutoCancelHoursDefault(24);
        }
    }

    private void validatePolicy(OrderPolicyRequest request, boolean strict) {
        if (strict && request.contractRequired() == null) {
            throw new BusinessException("error.policy.contract.required");
        }
        if (strict && request.lockPolicy() == null) {
            throw new BusinessException("error.policy.lock.required");
        }
        if (strict && request.paymentPolicy() == null) {
            throw new BusinessException("error.policy.payment.required");
        }

        if (request.depositValue() != null && request.depositValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("error.policy.depositValue.positive");
        }
        if (request.depositDeadlineDays() != null && request.depositDeadlineDays() < 0) {
            throw new BusinessException("error.policy.depositDeadline.nonNegative");
        }
        if (request.balanceDeadlineDays() != null && request.balanceDeadlineDays() < 0) {
            throw new BusinessException("error.policy.balanceDeadline.nonNegative");
        }
        if (request.autoCancelHours() != null && request.autoCancelHours() < 0) {
            throw new BusinessException("error.policy.autoCancelHours.nonNegative");
        }
        if (request.paymentPolicy() == OrderPaymentPolicy.DEPOSIT_BALANCE) {
            if (request.depositType() == null || request.depositValue() == null) {
                throw new BusinessException("error.policy.deposit.typeValue.required");
            }
            if (request.depositType() == DepositRuleType.PERCENT
                && request.depositValue().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("error.policy.depositPercent.max100");
            }
        }
    }

    private BigDecimal scaleMoney(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void applyPrice(DeparturePrice price, DeparturePriceRequest request) {
        String rawType = request.priceType() == null ? "" : request.priceType().trim();
        if (rawType.isBlank()) {
            throw new BusinessException("error.departure.priceType.required");
        }
        String normalizedType = STANDARD_PRICE_TYPES.contains(rawType.toUpperCase(Locale.ROOT))
            ? rawType.toUpperCase(Locale.ROOT)
            : rawType;
        if (request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("error.price.invalid");
        }
        if (request.currency() != null && !request.currency().isBlank()
            && !CNY.equalsIgnoreCase(request.currency().trim())) {
            throw new BusinessException("error.currency.onlyCnySupported");
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

