package ru.seller_support.assignment.adapter.marketplace.yandexmarket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Delivery;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Item;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class YandexMarketOrderSplitter {

    public List<Order> splitOrders(List<Order> orders) {
        List<Order> result = new ArrayList<>();

        for (Order order : orders) {
            List<Delivery.Box> boxes = order.getDelivery().getShipments().getFirst().getBoxes();
            if (order.getItems().size() <= 1) {
                List<Order> splittedOrders = splitSameItemsByBoxesIfNecessary(order, boxes);
                result.addAll(splittedOrders);
            } else {
                List<Order> splittedOrders = splitDifferentItemsByBoxesIfNecessary(order, boxes);
                result.addAll(splittedOrders);
            }
        }

        return result;
    }

    private List<Order> splitDifferentItemsByBoxesIfNecessary(Order order, List<Delivery.Box> boxes) {
        List<Order> result = new ArrayList<>();
        if (boxes.size() == 1) {
            for (Item item : order.getItems()) {
                Order newOrder = order.toBuilder()
                        .items(Collections.singletonList(item))
                        .originalId(order.getId())
                        .build();
                result.add(newOrder);
            }
            return result;
        }
        try {
            List<Item> items = order.getItems();
            int countOfItems = items.stream().map(Item::getCount).reduce(0, Integer::sum);
            if (countOfItems % boxes.size() != 0) {
                throw new ArithmeticException(String.format("Кол-во товаров с разными артикулами, а именно %s, в заказе %s не делится без остатка на кол-во грузомест %s",
                        countOfItems, order.getId(), boxes.size()));
            }
            int currentBoxIndex = 0;
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                int countOfItem = item.getCount();
                while (countOfItem > 1) {
                    countOfItem--;
                    item.setCount(countOfItem);
                    Item newItem = item.toBuilder()
                            .count(1)
                            .build();
                    Order newOrder = order.toBuilder()
                            .id(boxes.get(currentBoxIndex).getFulfilmentId())
                            .items(Collections.singletonList(newItem))
                            .originalId(order.getId())
                            .build();
                    result.add(newOrder);
                    currentBoxIndex++;
                }
                if (countOfItem == 1) {
                    Order newOrder = order.toBuilder()
                            .id(boxes.get(currentBoxIndex).getFulfilmentId())
                            .items(Collections.singletonList(item))
                            .originalId(order.getId())
                            .build();
                    result.add(newOrder);
                    currentBoxIndex++;
                }
            }
            return result;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            for (Item item : order.getItems()) {
                Order newOrder = order.toBuilder()
                        .items(Collections.singletonList(item))
                        .wrongBox(true)
                        .build();
                result.add(newOrder);
            }
            return result;
        }
    }


    private List<Order> splitSameItemsByBoxesIfNecessary(Order order, List<Delivery.Box> boxes) {
        if (boxes.size() == 1) {
            order.setOriginalId(order.getId());
            return List.of(order);
        }
        try {
            List<Order> result = new ArrayList<>();
            Item originalItem = order.getItems().getFirst();
            if (originalItem.getCount() % boxes.size() != 0) {
                throw new ArithmeticException(String.format("Кол-во одинаковых товаров, а именно %s, в заказе %s не делится без остатка на кол-во грузомест %s",
                        originalItem.getCount(), order.getId(), boxes.size()));
            }
            int countOfItemInBox = originalItem.getCount() / boxes.size();
            for (int i = 0; i < boxes.size(); i++) {
                Item updatedItem = originalItem.toBuilder()
                        .count(countOfItemInBox)
                        .build();
                Order newOrder = order.toBuilder()
                        .id(boxes.get(i).getFulfilmentId())
                        .items(Collections.singletonList(updatedItem))
                        .originalId(order.getId())
                        .build();
                result.add(newOrder);
            }
            return result;
        } catch (ArithmeticException e) {
            log.warn(e.getMessage(), e);
            order.setWrongBox(true);
            return List.of(order);
        }
    }
}
