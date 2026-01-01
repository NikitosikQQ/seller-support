package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.adapter.marketplace.wb.WildberriesAdapter;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderRepository;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.service.ShopService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.seller_support.assignment.domain.enums.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusRefresher {

    private static final List<OrderStatus> STATUSES_FOR_ACTUALIZE_WB = List.of(CREATED, PILA, CHPU, KROMKA, UPAKOVKA);
    private static final List<OrderStatus> STATUSES_FOR_REFRESH = List.of(PILA, CHPU, KROMKA, UPAKOVKA);
    private static final int MAX_DAYS_WITHOUT_CHANGES = 7;
    private static final int DAYS_FOR_ACTUALIZE_WB_STATUS = 2;
    private static final int DEFAULT_BATCH_SIZE = 100;

    private static final String WILDBERRIES_AUTHOR = "WILDBERRIES";
    private static final String SYSTEM_AUTHOR = "SYSTEM_AUTO_REFRESH";
    private static final LocalDateTime MIN_CREATED_DATE = LocalDateTime.of(2025, 12, 1, 0, 0, 0);

    private final OrderRepository orderRepository;
    private final OrderStatusHandler statusHandler;
    private final WildberriesAdapter wildberriesAdapter;
    private final ShopService shopService;
    private final Clock clock;

    //todo если появятся вебхуки у вб, переписать на них по аналогии с озон и яндекс
    public void actualizeWildberriesOrderStatus() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusDays(DAYS_FOR_ACTUALIZE_WB_STATUS);

        log.info("Старт актуализации статусов заказов ВБ");
        try {
            List<String> numbers = orderRepository.findOrderNumbersForRefresh(
                    STATUSES_FOR_ACTUALIZE_WB,
                    threshold,
                    List.of(Marketplace.WILDBERRIES),
                    MIN_CREATED_DATE
            );

            if (CollectionUtils.isEmpty(numbers)) {
                log.info("Не найдено заказов WB на обновление");
                return;
            }
            log.info("Найдено {} ВБ заказов на актуализацию", numbers.size());

            for (int i = 0; i < numbers.size(); i += DEFAULT_BATCH_SIZE) {
                List<String> batchOfNumbers = numbers.subList(i, Math.min(i + DEFAULT_BATCH_SIZE, numbers.size()));
                List<OrderEntity> orders = orderRepository.findByNumberIn(batchOfNumbers);

                Map<String, OrderEntity> orderNumberEntityMap = orders.stream()
                        .collect(Collectors.toMap(OrderEntity::getNumber, Function.identity()));

                Map<String, List<OrderEntity>> groupedByShopName = orders.stream()
                        .collect(Collectors.groupingBy(OrderEntity::getShopName));
                Map<ShopEntity, List<OrderEntity>> groupedByShopEntity = getGroupedOrdersByShopEntity(groupedByShopName);

                for (var entry : groupedByShopEntity.entrySet()) {
                    ShopEntity shop = entry.getKey();
                    List<OrderEntity> shopOrders = entry.getValue();

                    log.info("Запрос статусов в Wildberries для магазина '{}' (заказов: {})", shop.getName(), shopOrders.size());

                    Map<String, OrderStatus> externalStatuses = wildberriesAdapter.getOrderStatuses(shop, shopOrders);

                    for (var extEntry : externalStatuses.entrySet()) {
                        String orderNumber = extEntry.getKey();
                        OrderStatus newStatus = extEntry.getValue();

                        OrderEntity currentOrder = orderNumberEntityMap.get(orderNumber);
                        if (currentOrder == null) {
                            log.warn("Получен статус для заказа '{}' который отсутствует в текущей выборке", orderNumber);
                            continue;
                        }

                        statusHandler.updateStatusAndSave(currentOrder, newStatus, WILDBERRIES_AUTHOR);
                    }
                }
                Thread.sleep(200);
            }

            log.info("Актуализация статусов WB заказов завершено успешно.");
        } catch (Exception ex) {
            log.error("Во время обновления статусов WB заказов произошла ошибка {}", ex.getMessage(), ex);
        }
    }

    public void refreshOrdersStatus() {
        log.info("Перезапуск статусной модели по зависшим заказам");

        LocalDateTime threshold = LocalDateTime.now(clock).minusDays(MAX_DAYS_WITHOUT_CHANGES);
        try {
            List<String> numbers = orderRepository.findOrderNumbersForRefresh(
                    STATUSES_FOR_REFRESH,
                    threshold,
                    List.of(Marketplace.YANDEX_MARKET, Marketplace.OZON, Marketplace.WILDBERRIES),
                    MIN_CREATED_DATE
            );

            if (CollectionUtils.isEmpty(numbers)) {
                log.info("Не найдено заказов на перезапуск статусной модели");
                return;
            }
            log.info("Найдено заказов на перезапуск {}", numbers.size());

            for (int i = 0; i < numbers.size(); i += DEFAULT_BATCH_SIZE) {
                List<String> batchOfNumbers = numbers.subList(i, Math.min(i + DEFAULT_BATCH_SIZE, numbers.size()));
                List<OrderEntity> orders = orderRepository.findByNumberIn(batchOfNumbers);
                statusHandler.updateOrdersStatusAndSave(orders, CREATED, SYSTEM_AUTHOR);
            }

            log.info("Перезапуск статусной модели зависших заказов завершен успешно.");
        } catch (Exception ex) {
            log.error("Во время перезапуска статусов заказов произошла ошибка {}", ex.getMessage(), ex);
        }
    }

    private Map<ShopEntity, List<OrderEntity>> getGroupedOrdersByShopEntity(Map<String, List<OrderEntity>> groupedByShopName) {
        Map<String, ShopEntity> shops = shopService.findAllByNames(groupedByShopName.keySet()).stream()
                .collect(Collectors.toMap(ShopEntity::getName, Function.identity()));
        Map<ShopEntity, List<OrderEntity>> groupedByShopEntity = new HashMap<>();

        for (var entry : groupedByShopName.entrySet()) {
            String shopName = entry.getKey();
            List<OrderEntity> list = entry.getValue();
            ShopEntity shop = shops.get(shopName);
            if (shop != null) {
                groupedByShopEntity.put(shop, list);
            } else {
                log.warn("Не удалось найти магазин с именем '{}' для актуализации вб заказов", shopName);
            }
        }

        return groupedByShopEntity;
    }
}

