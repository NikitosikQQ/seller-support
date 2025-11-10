package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.employee.ProcessEmployeeWorkRequest;
import ru.seller_support.assignment.controller.dto.response.EmployeeCapacityDtoResponse;
import ru.seller_support.assignment.domain.enums.RoleNames;
import ru.seller_support.assignment.service.employee.EmployeeCapacityService;
import ru.seller_support.assignment.service.employee.EmployeeWorkProcessService;
import ru.seller_support.assignment.service.mapper.EmployeeProcessedCapacityMapper;
import ru.seller_support.assignment.util.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
@Slf4j
public class EmployeeController {

    private final EmployeeWorkProcessService employeeWorkProcessService;
    private final EmployeeCapacityService employeeCapacityService;

    private final EmployeeProcessedCapacityMapper mapper;

    @PostMapping(path = "/work/process")
    public ResponseEntity<String> processWork(@Valid @RequestBody ProcessEmployeeWorkRequest request) {
        log.info("Производится процесс обработки заказа: {} ", request);
        var result = employeeWorkProcessService.processWork(request);
        if (result.updated()) {
            log.info("Обработка заказа успешно завершена. Статус обновлен: {} ", request);
        } else {
            log.info("Обработка заказа завершена без обновления статуса: {} ", request);
        }
        return ResponseEntity.ok().body(result.alert());
    }

    @GetMapping(path = "/capacity/actual")
    public ResponseEntity<List<EmployeeCapacityDtoResponse>> getActualCapacity(
            @RequestParam(required = false) String workplace) {
        var isAdmin = SecurityUtils.getRoles().contains(RoleNames.ADMIN);
        var capacity = employeeCapacityService.getActualCapacity(isAdmin, workplace);
        var response = capacity.stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
