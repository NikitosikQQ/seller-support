package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.ColorEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderChangesHistoryEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderChangesHistoryRepository;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderRepository;
import ru.seller_support.assignment.controller.dto.request.order.SearchChpuOrderRequest;
import ru.seller_support.assignment.controller.dto.request.order.SearchOrderRequest;
import ru.seller_support.assignment.controller.dto.response.ChpuOrderDto;
import ru.seller_support.assignment.service.ColorService;
import ru.seller_support.assignment.service.MaterialService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;
import static ru.seller_support.assignment.adapter.postgres.entity.order.OrderSpecifications.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderSearchService {

    private static final Sort SORT_IN_PROCESS_AT_ASC = Sort.by(Sort.Direction.ASC, "inProcessAt");

    private final OrderRepository orderRepository;
    private final OrderChangesHistoryRepository orderHistoryRepository;
    private final MaterialService materialService;
    private final ColorService colorService;

    @Value("${app.orders.dimensionalTolerance}")
    private Integer dimensionalTolerance;

    @Transactional(readOnly = true)
    public List<OrderChangesHistoryEntity> getHistoryByOrderNumber(String orderNumber) {
        return orderHistoryRepository.findByOrderNumberOrderByCreatedAt(orderNumber);
    }

    public Page<OrderEntity> search(SearchOrderRequest query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(), SORT_IN_PROCESS_AT_ASC);

        Specification<OrderEntity> spec = generateSpecFromSearchQuery(query);

        return orderRepository.findAll(spec, pageable);
    }

    public List<OrderEntity> searchWithoutPage(SearchOrderRequest query) {
        Specification<OrderEntity> spec = generateSpecFromSearchQuery(query);

        return orderRepository.findAll(spec, SORT_IN_PROCESS_AT_ASC);
    }

    public Specification<OrderEntity> generateSpecFromSearchQuery(SearchOrderRequest query) {
        return where(statusIn(query.getStatuses()))
                .and(inProcessAtBetween(query.getFromInProcessAt(), query.getToInProcessAt()))
                .and(dimensionsLessThanOrEqual(query.getLength(), query.getWidth(), dimensionalTolerance))
                .and(thicknessEqual(query.getThickness()))
                .and(marketplacesIn(query.getMarketplaces()))
                .and(numberEqual(query.getNumber()))
                .and(colorNumberEqual(query.getColorNumber()))
                .and(materialNameEqual(query.getMaterialName()))
                .and(materialNameIn(query.getMaterialNames()))
                .and(shopNameEqual(query.getShopName()))
                .and(materialNameNotIn(query.getExcludeMaterialNames()));
    }

    public List<ChpuOrderDto> searchChpu(SearchChpuOrderRequest query) {
        List<String> chpuMaterialNames = materialService.findAllForChpu().stream()
                .map(MaterialEntity::getName)
                .toList();

        Specification<OrderEntity> spec = where(statusIn(query.getStatuses()))
                .and(thicknessEqual(query.getThickness()))
                .and(materialNameIn(chpuMaterialNames))
                .and(materialNameNotIn(query.getExcludeMaterialNames()));

        List<OrderEntity> found = orderRepository.findAll(spec);

        // Группируем по материалу, толщине и номеру цвета
        Map<String, List<OrderEntity>> grouped = found.stream()
                .collect(Collectors.groupingBy(order ->
                        order.getMaterialName() + "_" + order.getThickness() + "_" + order.getColorNumber()
                ));
        Map<Integer, String> colors = colorService.findAll().stream()
                .collect(Collectors.toMap(ColorEntity::getNumber, ColorEntity::getName));

        return grouped.values().stream()
                .map(orders -> mapToChpuDto(orders, colors))
                .sorted(Comparator.comparing(ChpuOrderDto::getAreaSummary).reversed())
                .toList();
    }

    private ChpuOrderDto mapToChpuDto(List<OrderEntity> orders, Map<Integer, String> colors) {
        BigDecimal areaSummary = orders.stream()
                .map(OrderEntity::getAreaInMeters)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> orderNumbers = orders.stream()
                .map(OrderEntity::getNumber)
                .distinct()
                .toList();


        OrderEntity first = orders.getFirst();
        String color = colors.getOrDefault(first.getColorNumber(), "Бесцветный");

        String shortArticle = String.format("%s %d %s",
                first.getMaterialName(),
                first.getThickness(),
                color
        );

        return ChpuOrderDto.builder()
                .shortArticle(shortArticle)
                .areaSummary(areaSummary)
                .orderNumbers(orderNumbers)
                .build();
    }
}
