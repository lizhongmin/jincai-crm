package com.jincai.crm.order.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.repository.TravelerRepository;
import com.jincai.crm.order.dto.OrderPriceSelectionRequest;
import com.jincai.crm.order.dto.OrderRequest;
import com.jincai.crm.order.entity.OrderPriceItem;
import com.jincai.crm.order.entity.OrderTravelerSnapshot;
import com.jincai.crm.order.repository.OrderPriceItemRepository;
import com.jincai.crm.order.repository.OrderTravelerSnapshotRepository;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.repository.DeparturePriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单价格计算服务。
 *
 * <p>负责订单的价格计算、旅客快照创建、价格项生成等核心定价逻辑。
 * 从庞大的 {@link OrderService} 中拆分出来，以提高代码的可维护性和可测试性。
 *
 * <p>主要职责：
 * <ul>
 *   <li>根据旅客信息和价格规则计算订单总价</li>
 *   <li>创建旅客快照（OrderTravelerSnapshot）</li>
 *   <li>生成订单价格项（OrderPriceItem）</li>
 *   <li>处理价格选择和验证</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPricingService {

    private static final String CNY = "CNY";

    private final TravelerRepository travelerRepository;
    private final DeparturePriceRepository priceRepository;
    private final OrderTravelerSnapshotRepository travelerSnapshotRepository;
    private final OrderPriceItemRepository orderPriceItemRepository;

    /**
     * 根据订单请求计算价格。
     *
     * @param request 订单请求
     * @return 价格计算结果
     */
    public PricingCalculation calculatePricing(OrderRequest request) {
        List<Traveler> travelers = travelerRepository.findByCustomerIdAndDeletedFalse(request.customerId());
        if (travelers.isEmpty()) {
            throw new BusinessException("error.order.traveler.notFound");
        }

        List<DeparturePrice> prices = priceRepository.findByDepartureIdAndDeletedFalse(request.departureId());
        if (prices.isEmpty()) {
            throw new BusinessException("error.departure.price.notFound");
        }

        List<OrderTravelerSnapshot> travelerSnapshots = new ArrayList<>();
        List<OrderPriceItem> priceItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Traveler traveler : travelers) {
            // 为每个旅客计算价格
            List<DeparturePrice> matchedPrices = matchPricesForTraveler(prices, traveler, request.priceSelections());
            for (DeparturePrice price : matchedPrices) {
                OrderPriceItem item = new OrderPriceItem();
                item.setDeparturePriceId(price.getId());
                item.setPriceType(price.getPriceType());
                item.setItemName(price.getPriceLabel() != null ? price.getPriceLabel() : price.getPriceType());
                item.setAmount(price.getPrice());
                item.setQuantity(1);
                item.setUnitPrice(price.getPrice());
                item.setCurrency(CNY);
                priceItems.add(item);
                totalAmount = totalAmount.add(price.getPrice());
            }

            // 创建旅客快照
            OrderTravelerSnapshot snapshot = new OrderTravelerSnapshot();
            snapshot.setTravelerId(traveler.getId());
            snapshot.setName(traveler.getName());
            snapshot.setPhone(traveler.getPhone());
            snapshot.setIdType(traveler.getIdType());
            snapshot.setIdNo(traveler.getIdNo());
            travelerSnapshots.add(snapshot);
        }

        return new PricingCalculation(
            request.routeId(),
            request.departureId(),
            travelerSnapshots,
            priceItems,
            totalAmount,
            CNY
        );
    }

    /**
     * 为指定订单保存价格计算结果。
     *
     * @param orderId 订单ID
     * @param calculation 价格计算结果
     */
    @Transactional
    public void persistPricing(String orderId, PricingCalculation calculation) {
        // 保存旅客快照
        for (OrderTravelerSnapshot snapshot : calculation.travelerSnapshots()) {
            snapshot.setOrderId(orderId);
            travelerSnapshotRepository.save(snapshot);
        }

        // 保存价格项
        for (OrderPriceItem item : calculation.priceItems()) {
            item.setOrderId(orderId);
            orderPriceItemRepository.save(item);
        }
    }

    /**
     * 替换指定订单的价格计算结果（先删除再保存）。
     *
     * @param orderId 订单ID
     * @param calculation 新的价格计算结果
     */
    @Transactional
    public void replacePricing(String orderId, PricingCalculation calculation) {
        // 删除旧的定价信息
        markPricingDeleted(orderId);

        // 保存新的定价信息
        persistPricing(orderId, calculation);
    }

    /**
     * 将指定订单的价格项标记为已删除。
     *
     * @param orderId 订单ID
     */
    @Transactional
    public void markPricingDeleted(String orderId) {
        List<OrderTravelerSnapshot> snapshots = travelerSnapshotRepository.findByOrderIdAndDeletedFalse(orderId);
        for (OrderTravelerSnapshot snapshot : snapshots) {
            snapshot.setDeleted(true);
            travelerSnapshotRepository.save(snapshot);
        }

        List<OrderPriceItem> items = orderPriceItemRepository.findByOrderIdAndDeletedFalse(orderId);
        for (OrderPriceItem item : items) {
            item.setDeleted(true);
            orderPriceItemRepository.save(item);
        }
    }

    // ------------------------------------------------------------------ private

    /**
     * 为单个旅客匹配适用的价格。
     *
     * <p>匹配规则：
     * <ul>
     *   <li>根据旅客年龄匹配价格类型（ADULT/CHILD/INFANT）</li>
     *   <li>根据价格选择请求精确匹配</li>
     *   <li>支持附加费项目</li>
     * </ul>
     *
     * @param availablePrices 可用价格列表
     * @param traveler 旅客信息
     * @param selections 价格选择请求
     * @return 匹配到的价格列表
     */
    private List<DeparturePrice> matchPricesForTraveler(
        List<DeparturePrice> availablePrices,
        Traveler traveler,
        List<OrderPriceSelectionRequest> selections) {

        List<DeparturePrice> matched = new ArrayList<>();

        // 按价格类型分组可用价格
        Map<String, List<DeparturePrice>> pricesByType = availablePrices.stream()
            .collect(Collectors.groupingBy(DeparturePrice::getPriceType));

        // 基础价格匹配（根据年龄）
        String basePriceType = determinePriceTypeByAge(traveler.getBirthday());
        List<DeparturePrice> basePrices = pricesByType.getOrDefault(basePriceType, List.of());
        if (!basePrices.isEmpty()) {
            // 优先选择第一个匹配的价格
            matched.add(basePrices.get(0));
        }

        // 附加价格匹配（如单房差、附加费等）
        if (selections != null) {
            for (OrderPriceSelectionRequest selection : selections) {
                // 直接通过ID查找特定的价格项
                for (DeparturePrice price : availablePrices) {
                    if (price.getId().equals(selection.departurePriceId())) {
                        matched.add(price);
                        break;
                    }
                }
            }
        }

        return matched;
    }

    /**
     * 根据旅客生日计算其价格类型。
     *
     * <p>规则：
     * <ul>
     *   <li>2岁以下：INFANT（婴儿）</li>
     *   <li>2-12岁：CHILD（儿童）</li>
     *   <li>12岁以上：ADULT（成人）</li>
     * </ul>
     *
     * @param birthday 旅客生日
     * @return 价格类型（ADULT/CHILD/INFANT）
     */
    private String determinePriceTypeByAge(java.time.LocalDate birthday) {
        if (birthday == null) {
            return "ADULT"; // 默认成人价
        }

        Period age = Period.between(birthday, java.time.LocalDate.now());
        int years = age.getYears();

        if (years < 2) {
            return "INFANT";
        } else if (years <= 12) {
            return "CHILD";
        } else {
            return "ADULT";
        }
    }
}