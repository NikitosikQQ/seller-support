package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderChangesHistoryRepository;
import ru.seller_support.assignment.adapter.postgres.repository.order.OrderRepository;
import ru.seller_support.assignment.controller.dto.request.employee.EmployeeDto;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.mapper.OrderMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static ru.seller_support.assignment.domain.enums.OrderStatus.FINAL_STATUSES;
import static ru.seller_support.assignment.service.mapper.OrderMapper.DEFAULT_SYSTEM_AUTHOR;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusHandler {

    private final OrderRepository orderRepository;
    private final OrderChangesHistoryRepository orderChangesHistoryRepository;
    private final OrderMapper orderMapper;
    private final Clock clock;

    @Transactional
    public void updateOrdersStatusAndSave(List<OrderEntity> orders,
                                          OrderStatus newStatus,
                                          String author) {
        orders.forEach(order -> updateStatusAndSave(order, newStatus, author));
    }

    @Transactional
    public OrderUpdateStatusResult updateStatusAndSave(OrderEntity order,
                                                       OrderStatus newStatus,
                                                       String author) {
        if (FINAL_STATUSES.contains(order.getStatus())) {
            return OrderUpdateStatusResult.failedUpdate(order);
        }
        if (order.getStatus().equals(newStatus)) {
            return OrderUpdateStatusResult.successUpdate(order);
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now(clock));

        var saved = orderRepository.save(order);
        saveOrderHistory(order, author, null);

        return OrderUpdateStatusResult.successUpdate(saved);
    }

    @Transactional
    public OrderUpdateStatusResult updateStatusAndSave(OrderEntity order,
                                                       OrderStatus newStatus,
                                                       List<EmployeeDto> employees) {
        var firstEmployeeWorkplace = Workplace.fromValue(employees.getFirst().getWorkplace());
        if (!order.getStatus().canUpdateToNewStatus(newStatus, firstEmployeeWorkplace)) {
            return OrderUpdateStatusResult.failedUpdate(order);
        }

        var saved = updateStatusWithHistory(order, newStatus, employees);
        return OrderUpdateStatusResult.successUpdate(saved);
    }

    private OrderEntity updateStatusWithHistory(OrderEntity order, OrderStatus newStatus, List<EmployeeDto> employees) {
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now(clock));

        employees.forEach(employee ->
                saveOrderHistory(order, employee.getUsername(), Workplace.fromValue(employee.getWorkplace()
                )));

        if (newStatus == OrderStatus.BRAK) {
            order.setStatus(OrderStatus.CREATED);
            saveOrderHistory(order, DEFAULT_SYSTEM_AUTHOR, null);
        }


        return orderRepository.save(order);
    }

    private void saveOrderHistory(OrderEntity order, String author, Workplace workplace) {
        var orderHistory = orderMapper.toOrderHistory(order, author, workplace);

        orderChangesHistoryRepository.save(orderHistory.setCreatedAt(LocalDateTime.now(clock)));
    }


    public record OrderUpdateStatusResult(OrderEntity order, boolean wasUpdate) {
        public static OrderUpdateStatusResult successUpdate(OrderEntity order) {
            return new OrderUpdateStatusResult(order, true);
        }

        public static OrderUpdateStatusResult failedUpdate(OrderEntity order) {
            return new OrderUpdateStatusResult(order, false);
        }
    }
}
