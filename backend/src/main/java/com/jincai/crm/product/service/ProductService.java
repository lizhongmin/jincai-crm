package com.jincai.crm.product.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.order.entity.DepositRuleType;
import com.jincai.crm.order.entity.OrderLockPolicy;
import com.jincai.crm.order.entity.OrderPaymentPolicy;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.DeparturePriceRepository;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.repository.RouteProductRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

@Service
@Slf4j
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

    public PageResult<RouteProduct> pageRoutes(int page, int size, String keyword) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<RouteProduct> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("name"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("code"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("departureCity"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("destinationCity"), "")), likeValue)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<RouteProduct> result = routeRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        return new PageResult<>(result.getContent(), result.getTotalElements(), normalizedPage, normalizedSize);
    }

    @Transactional
    public RouteProduct createRoute(@Valid RouteRequest request) {
        log.info("创建线路产品 - 线路名称: {}, 类别: {}, 出发城市: {}", request.name(), request.category(), request.departureCity());
        try {
            RouteProduct route = new RouteProduct();
            applyRoute(route, request);
            RouteProduct saved = routeRepository.save(route);
            log.info("线路产品创建成功 - 线路ID: {}, 线路代码: {}", saved.getId(), saved.getCode());
            return saved;
        } catch (Exception e) {
            log.error("创建线路产品失败 - 线路名称: {}", request.name(), e);
            throw e;
        }
    }

    @Transactional
    public RouteProduct updateRoute(String id, @Valid RouteRequest request) {
        log.info("更新线路产品 - 线路ID: {}, 线路名称: {}", id, request.name());
        try {
            RouteProduct route = routeRepository.findById(id).orElseThrow(() -> new BusinessException("error.route.notFound"));
            applyRoute(route, request);
            RouteProduct saved = routeRepository.save(route);
            log.info("线路产品更新成功 - 线路ID: {}, 线路代码: {}", id, saved.getCode());
            return saved;
        } catch (Exception e) {
            log.error("更新线路产品失败 - 线路ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteRoute(String id) {
        RouteProduct route = routeRepository.findById(id).orElseThrow(() -> new BusinessException("error.route.notFound"));
        route.setDeleted(true);
        routeRepository.save(route);
    }

    public List<Departure> departures(String routeId) {
        if (routeId != null) {
            return departureRepository.findByRouteIdAndDeletedFalse(routeId);
        }
        return departureRepository.findByDeletedFalse();
    }

    public PageResult<Departure> pageDepartures(int page, int size, String routeId, String keyword) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<Departure> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (routeId != null) {
                predicates.add(cb.equal(root.get("routeId"), routeId));
            }
            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("code"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("status"), "")), likeValue)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Departure> result = departureRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        return new PageResult<>(result.getContent(), result.getTotalElements(), normalizedPage, normalizedSize);
    }

    @Transactional
    public Departure createDeparture(@Valid DepartureRequest request) {
        log.info("创建出团日期 - 线路ID: {}, 出团日期: {}, 状态: {}", request.routeId(), request.startDate(), request.status());
        try {
            routeRepository.findById(request.routeId()).orElseThrow(() -> new BusinessException("error.route.notFound"));
            Departure departure = new Departure();
            applyDeparture(departure, request);
            Departure saved = departureRepository.save(departure);
            log.info("出团日期创建成功 - 出团ID: {}, 出团代码: {}", saved.getId(), saved.getCode());
            return saved;
        } catch (Exception e) {
            log.error("创建出团日期失败 - 线路ID: {}, 出团日期: {}", request.routeId(), request.startDate(), e);
            throw e;
        }
    }

    @Transactional
    public Departure updateDeparture(String id, @Valid DepartureRequest request) {
        log.info("更新出团日期 - 出团ID: {}, 线路ID: {}, 出团日期: {}", id, request.routeId(), request.startDate());
        try {
            routeRepository.findById(request.routeId()).orElseThrow(() -> new BusinessException("error.route.notFound"));
            Departure departure = departureRepository.findById(id).orElseThrow(() -> new BusinessException("error.departure.notFound"));
            applyDeparture(departure, request);
            Departure saved = departureRepository.save(departure);
            log.info("出团日期更新成功 - 出团ID: {}, 出团代码: {}", id, saved.getCode());
            return saved;
        } catch (Exception e) {
            log.error("更新出团日期失败 - 出团ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteDeparture(String id) {
        log.info("删除出团日期 - 出团ID: {}", id);
        try {
            Departure departure = departureRepository.findById(id).orElseThrow(() -> new BusinessException("error.departure.notFound"));
            departure.setDeleted(true);
            departureRepository.save(departure);
            log.info("出团日期删除成功 - 出团ID: {}", id);
        } catch (Exception e) {
            log.error("删除出团日期失败 - 出团ID: {}", id, e);
            throw e;
        }
    }

    public List<DeparturePrice> prices(String departureId) {
        return priceRepository.findByDepartureIdAndDeletedFalse(departureId);
    }

    public OrderPolicyView routePolicy(String routeId) {
        RouteProduct route = routeRepository.findById(routeId).orElseThrow(() -> new BusinessException("error.route.notFound"));
        normalizeRoutePolicyDefaults(route);
        return toOrderPolicyView(route);
    }

    @Transactional
    public OrderPolicyView updateRoutePolicy(String routeId, @Valid OrderPolicyRequest request) {
        RouteProduct route = routeRepository.findById(routeId).orElseThrow(() -> new BusinessException("error.route.notFound"));
        applyRoutePolicy(route, request);
        RouteProduct saved = routeRepository.save(route);
        return toOrderPolicyView(saved);
    }

    public DepartureOrderPolicyView departurePolicy(String departureId) {
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
    public DepartureOrderPolicyView updateDeparturePolicy(String departureId, @Valid OrderPolicyRequest request) {
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
    public DeparturePrice addPrice(String departureId, @Valid DeparturePriceRequest request) {
        log.info("添加出团价格 - 出团ID: {}, 价格类型: {}, 价格: {}", departureId, request.priceType(), request.price());
        try {
            departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
            DeparturePrice price = new DeparturePrice();
            price.setDepartureId(departureId);
            applyPrice(price, request);
            DeparturePrice saved = priceRepository.save(price);
            log.info("出团价格添加成功 - 价格ID: {}, 出团ID: {}", saved.getId(), departureId);
            return saved;
        } catch (Exception e) {
            log.error("添加出团价格失败 - 出团ID: {}", departureId, e);
            throw e;
        }
    }

    @Transactional
    public DeparturePrice updatePrice(String departureId, String priceId, @Valid DeparturePriceRequest request) {
        log.info("更新出团价格 - 价格ID: {}, 出团ID: {}, 价格类型: {}, 价格: {}", priceId, departureId, request.priceType(), request.price());
        try {
            departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
            DeparturePrice price = priceRepository.findById(priceId).orElseThrow(() -> new BusinessException("error.price.notFound"));
            applyPrice(price, request);
            DeparturePrice saved = priceRepository.save(price);
            log.info("出团价格更新成功 - 价格ID: {}, 出团ID: {}", priceId, departureId);
            return saved;
        } catch (Exception e) {
            log.error("更新出团价格失败 - 价格ID: {}, 出团ID: {}", priceId, departureId, e);
            throw e;
        }
    }

    @Transactional
    public void deletePrice(String departureId, String priceId) {
        log.info("删除出团价格 - 价格ID: {}, 出团ID: {}", priceId, departureId);
        try {
            departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
            DeparturePrice price = priceRepository.findById(priceId).orElseThrow(() -> new BusinessException("error.price.notFound"));
            price.setDeleted(true);
            priceRepository.save(price);
            log.info("出团价格删除成功 - 价格ID: {}, 出团ID: {}", priceId, departureId);
        } catch (Exception e) {
            log.error("删除出团价格失败 - 价格ID: {}, 出团ID: {}", priceId, departureId, e);
            throw e;
        }
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

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }
}

