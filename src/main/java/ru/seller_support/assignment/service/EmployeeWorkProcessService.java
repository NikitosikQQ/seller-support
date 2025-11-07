package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.controller.dto.request.employee.EmployeeDto;
import ru.seller_support.assignment.controller.dto.request.employee.ProcessEmployeeWorkRequest;
import ru.seller_support.assignment.domain.enums.CapacityOperationType;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.employee.EmployeeCapacityService;
import ru.seller_support.assignment.service.employee.EmployeeValidationService;
import ru.seller_support.assignment.service.order.OrderService;
import ru.seller_support.assignment.service.order.OrderStatusHandler;

import java.util.List;

import static ru.seller_support.assignment.domain.enums.Workplace.UPAKOVSHIK_WORKPLACES;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeWorkProcessService {

    private final OrderStatusHandler orderStatusHandler;
    private final OrderService orderService;
    private final OrderAlertService orderAlertService;
    private final EmployeeCapacityService employeeCapacityService;

    private final EmployeeValidationService employeeValidationService;

    @Transactional
    public EmployeeWorkResult processWork(ProcessEmployeeWorkRequest request) {
        employeeValidationService.validateEmployees(request.getEmployees());
        var employeesCount = request.getEmployees().size();
        return employeesCount > 1
                ? processWorkForPilaEmployees(request)
                : processWorkForOneEmployee(request.getOrderNumber(), request.getEmployees().getFirst(), request.getOperationType());
    }

    private EmployeeWorkResult processWorkForOneEmployee(String orderNumber, EmployeeDto employee, CapacityOperationType operationType) {
        var order = orderService.findByNumber(orderNumber);
        if (order == null) {
            return EmployeeWorkResult.notUpdated();
        }

        var previousOrderStatus = order.getStatus();

        var wasUpdated = updateOrderStatusIfNeededAndSave(order, List.of(employee), operationType);

        var needApplyFine = previousOrderStatus != OrderStatus.CREATED && operationType == CapacityOperationType.PENALTY;

        if (wasUpdated && (needApplyFine || operationType == CapacityOperationType.EARNING)) {
            employeeCapacityService.calculateAndSaveEmployeeCapacity(
                    order,
                    employee.getUsername(),
                    Workplace.fromValue(employee.getWorkplace()),
                    operationType);
        }
        var workplace = Workplace.fromValue(employee.getWorkplace());

        if (UPAKOVSHIK_WORKPLACES.contains(workplace)) {
            var alert = orderAlertService.getAlertByOrder(order);
            return new EmployeeWorkResult(wasUpdated, alert);
        }

        return new EmployeeWorkResult(wasUpdated, null);
    }

    private EmployeeWorkResult processWorkForPilaEmployees(ProcessEmployeeWorkRequest request) {
        var employees = request.getEmployees();
        var orderNumber = request.getOrderNumber();
        var operationType = request.getOperationType();

        var order = orderService.findByNumber(orderNumber);
        if (order == null) {
            return EmployeeWorkResult.notUpdated();
        }
        var previousOrderStatus = order.getStatus();

        var wasUpdated = updateOrderStatusIfNeededAndSave(order, employees, operationType);

        var needApplyFine = previousOrderStatus != OrderStatus.CREATED && operationType == CapacityOperationType.PENALTY;

        if (wasUpdated && (needApplyFine || operationType == CapacityOperationType.EARNING)) {
            employees.forEach(emp ->
                    employeeCapacityService.calculateAndSaveEmployeeCapacity(
                            order,
                            emp.getUsername(),
                            Workplace.fromValue(emp.getWorkplace()),
                            operationType)
            );
        }
        return new EmployeeWorkResult(wasUpdated, null);
    }

    private boolean updateOrderStatusIfNeededAndSave(OrderEntity order, List<EmployeeDto> employees, CapacityOperationType operationType) {
        var newStatus = operationType == CapacityOperationType.EARNING
                ? OrderStatus.SUCCESS_WORKPLACE_ORDER_STATUS_MAP.get(Workplace.fromValue(employees.getFirst().getWorkplace()))
                : OrderStatus.BRAK;
        var result = orderStatusHandler.updateStatusAndSave(order, newStatus, employees);
        return result.wasUpdate();
    }

    public record EmployeeWorkResult(boolean updated, String alert) {
        public static EmployeeWorkResult notUpdated() {
            return new EmployeeWorkResult(false, null);
        }
    }
}
