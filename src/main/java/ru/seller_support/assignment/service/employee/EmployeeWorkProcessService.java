package ru.seller_support.assignment.service.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.controller.dto.request.employee.EmployeeDto;
import ru.seller_support.assignment.controller.dto.request.employee.ProcessEmployeeWorkRequest;
import ru.seller_support.assignment.controller.dto.response.order.ResultInformationResponse;
import ru.seller_support.assignment.domain.enums.CapacityOperationType;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.order.OrderAlertService;
import ru.seller_support.assignment.service.order.OrderService;
import ru.seller_support.assignment.service.order.OrderStatusHandler;

import java.util.List;

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
    public ResultInformationResponse processWork(ProcessEmployeeWorkRequest request) {
        employeeValidationService.validateEmployees(request.getEmployees());
        var employeesCount = request.getEmployees().size();
        return employeesCount > 1
                ? processWorkForPilaEmployees(request)
                : processWorkForOneEmployee(request.getOrderNumber(), request.getEmployees().getFirst(), request.getOperationType());
    }

    private ResultInformationResponse processWorkForOneEmployee(String orderNumber, EmployeeDto employee, CapacityOperationType operationType) {
        var order = orderService.findByNumber(orderNumber);
        if (order == null) {
            return orderAlertService.getOrderNotFoundAlert(orderNumber);
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

        return orderAlertService.getResponseWithoutSoundAlert(wasUpdated, order);
    }

    private ResultInformationResponse processWorkForPilaEmployees(ProcessEmployeeWorkRequest request) {
        var employees = request.getEmployees();
        var orderNumber = request.getOrderNumber();
        var operationType = request.getOperationType();

        var order = orderService.findByNumber(orderNumber);
        if (order == null) {
            return orderAlertService.getOrderNotFoundAlert(orderNumber);
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
        return orderAlertService.getResponseWithoutSoundAlert(wasUpdated, order);
    }

    private boolean updateOrderStatusIfNeededAndSave(OrderEntity order, List<EmployeeDto> employees, CapacityOperationType operationType) {
        var newStatus = operationType == CapacityOperationType.EARNING
                ? OrderStatus.SUCCESS_WORKPLACE_ORDER_STATUS_MAP.get(Workplace.fromValue(employees.getFirst().getWorkplace()))
                : OrderStatus.BRAK;
        var result = orderStatusHandler.updateStatusAndSave(order, newStatus, employees);
        return result.wasUpdate();
    }
}
