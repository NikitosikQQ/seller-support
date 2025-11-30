package ru.seller_support.assignment.service.order;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderChangesHistoryEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderChangesHistoryRepository;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderRepository;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.service.mapper.OrderMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderChangesHistoryRepository changesHistoryRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusHandler statusHandler;
    private final Clock clock;

    @Transactional(readOnly = true)
    public OrderEntity findByNumber(String number) {
        return orderRepository.findByNumber(number);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findAllByExternalOrderNumber(String number) {
        return orderRepository.findAllByExternalOrderNumber(number);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findByNumbersIn(Collection<String> numbers) {
        return orderRepository.findByNumberIn(numbers);
    }

    @Transactional
    public List<OrderEntity> saveAll(List<PostingInfoModel> orders) {
        var entities = orders.stream()
                .map(orderMapper::toNewEntity)
                .toList();

        var orderNumbers = entities.stream()
                .map(OrderEntity::getNumber)
                .toList();

        var existingNumbers = orderRepository.findAllNumbersByOrderNumberIn(orderNumbers);

        var toSave = entities.stream()
                .filter(e -> !existingNumbers.contains(e.getNumber()))
                .toList();

        if (toSave.isEmpty()) {
            return Collections.emptyList();
        }

        var now = LocalDateTime.now(clock);

        toSave.forEach(order -> {
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
        });

        var saved = orderRepository.saveAll(toSave);

        var initialHistory = saved.stream()
                .map(it -> orderMapper.toInitialOrderHistory(it, now))
                .toList();
        changesHistoryRepository.saveAll(initialHistory);

        return saved;
    }

    @Transactional
    public int deleteCompletedOrdersCreatedBefore(LocalDateTime maxCreatedAt) {
        return orderRepository.cleanUp(maxCreatedAt, OrderStatus.FINAL_STATUSES);
    }

    @Transactional(readOnly = true)
    public List<OrderChangesHistoryEntity> getOrderChanges(OrderEntity orderEntity) {
        return changesHistoryRepository.findByOrderId(orderEntity.getId());
    }

    @Transactional
    public void updateStatus(String orderNumber, OrderStatus newStatus, String author) {
        var order = orderRepository.findByNumber(orderNumber);
        if (order == null) {
            throw new ValidationException(String.format("Не найден заказ с номером %s", orderNumber));
        }
        var result = statusHandler.updateStatusAndSave(order, newStatus, author);
        if (!result.wasUpdate()) {
            throw new ValidationException(String.format("Заказ находится в финальном статусе: %s", order.getStatus()));
        }
    }
}
