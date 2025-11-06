package ru.seller_support.assignment.service.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.service.employee.EmployeeCapacityService;
import ru.seller_support.assignment.service.order.OrderService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanUpProcessor {

    private final OrderService orderService;
    private final EmployeeCapacityService employeeCapacityService;

    private final Clock clock;

    @Value("${app.orders.ttl}")
    private Duration orderTtl;

    @Value("${app.employeeCapacity.ttl}")
    private Duration employeeCapacityTtl;

    public int cleanUpOrders() {
        LocalDateTime maxCreatedAt = LocalDateTime.now(clock).minus(orderTtl);
        return orderService.deleteCompletedOrdersCreatedBefore(maxCreatedAt);
    }

    public void cleanEmployeeCapacity() {
        LocalDate maxProcessedAt = LocalDateTime.now(clock).minus(employeeCapacityTtl).toLocalDate();
        employeeCapacityService.cleanUpEmployeeProcessedCapacity(maxProcessedAt);
    }
}
