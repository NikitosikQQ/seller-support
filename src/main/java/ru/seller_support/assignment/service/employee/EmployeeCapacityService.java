package ru.seller_support.assignment.service.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeProcessedCapacityEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderChangesHistoryEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.adapter.postgres.repository.employee.EmployeeActivityHistoryRepository;
import ru.seller_support.assignment.adapter.postgres.repository.employee.EmployeeProcessedCapacityRepository;
import ru.seller_support.assignment.controller.dto.response.EmployeeCapacityDto;
import ru.seller_support.assignment.domain.enums.CapacityOperationType;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.WorkplaceService;
import ru.seller_support.assignment.service.mapper.EmployeeProcessedCapacityMapper;
import ru.seller_support.assignment.service.order.OrderService;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static ru.seller_support.assignment.service.mapper.OrderMapper.DEFAULT_SYSTEM_AUTHOR;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeCapacityService {

    private static final Duration CAPACITY_UPDATE_DELAY = Duration.ofMinutes(60);

    private final WorkplaceService workplaceService;
    private final OrderService orderService;

    private final EmployeeProcessedCapacityMapper mapper;

    private final EmployeeProcessedCapacityRepository employeeCapacityRepository;
    private final EmployeeActivityHistoryRepository employeeActivityHistoryRepository;

    private final Clock clock;

    @Transactional
    public void calculateAndSaveEmployeeCapacity(OrderEntity orderEntity,
                                                 String username,
                                                 Workplace workplace,
                                                 CapacityOperationType capacityOperationType) {
        //мониторинг работ за день учитывается с 21 до 21
        var now = LocalDateTime.now(clock);
        var date = now.toLocalDate();
        var cutoff = LocalTime.of(21, 0);
        LocalDate processedAt = now.toLocalTime().isBefore(cutoff) ? date : date.plusDays(1);

        if (capacityOperationType == CapacityOperationType.EARNING) {
            earnAmountToEmployee(orderEntity, username, workplace, processedAt);
        } else if (capacityOperationType == CapacityOperationType.PENALTY) {
            fineEmployees(orderEntity, workplace);
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeCapacityDto> getActualCapacity(boolean isAdmin, String workplace) {
        var today = LocalDate.now(clock);
        if (isAdmin) {
            Set<Workplace> workplaces = Workplace.getWorkplacesGroupByWorkplace(Workplace.fromValue(workplace));
            var capacities = workplaces.isEmpty()
                    ? employeeCapacityRepository.getAllCapacitiesByProcessedAt(today)
                    : employeeCapacityRepository.getActualCapacitiesByWorkplace(today, workplaces);
            return capacities.stream()
                    .map(mapper::toDto)
                    .toList();

        } else {
            if (workplace == null) {
                log.warn("Работники должны обязательно указать рабочее место для мониторинга выполненных работ");
                return Collections.emptyList();
            }

            Set<Workplace> workplaces = Workplace.getWorkplacesGroupByWorkplace(Workplace.fromValue(workplace));
            //если попросят вернуть задержку отображения для работников
//            var timeLimit = LocalDateTime.now(clock).minus(CAPACITY_UPDATE_DELAY);
//            return employeeActivityHistoryRepository.getCapacitiesByWorkplacesWithDelay(today, workplaces, timeLimit);
            var capacities = employeeCapacityRepository.getActualCapacitiesByWorkplace(today, workplaces);
            return capacities.stream()
                    .map(mapper::toDto)
                    .toList();
        }
    }

    @Transactional
    public void cleanUpEmployeeProcessedCapacity(LocalDate maxProcessedAt) {
        int count = employeeCapacityRepository.cleanUp(maxProcessedAt);
        int countHistory = employeeActivityHistoryRepository.cleanUp(maxProcessedAt);
        log.info("Успешно удалено записей о выполненном объеме работников: {}, и историчных записей: {}", count, countHistory);
    }

    private void earnAmountToEmployee(OrderEntity orderEntity,
                                      String username,
                                      Workplace workplace,
                                      LocalDate processedAt) {
        var processedCapacityOptional = employeeCapacityRepository.findByUsernameAndProcessedAtAndWorkplace(username, processedAt, workplace);
        var processedCapacity = processedCapacityOptional.orElse(initialProcessedCapacity(processedAt, username, workplace));

        var currentEarnedAmount = calculateCurrentAmount(orderEntity, workplace);
        var totalEarnedAmount = processedCapacity.getEarnedAmount().add(currentEarnedAmount);

        var currentCapacity = orderEntity.getAreaInMeters();
        var totalCapacity = processedCapacity.getCapacity().add(currentCapacity);

        processedCapacity.setEarnedAmount(totalEarnedAmount);
        processedCapacity.setCapacity(totalCapacity);

        var saved = employeeCapacityRepository.save(processedCapacity);
        saveEmployeeActivityHistory(saved, CapacityOperationType.EARNING, currentCapacity, currentEarnedAmount);
    }

    private void fineEmployees(OrderEntity orderEntity, Workplace workplace) {
        var orderChanges = orderService.getOrderChanges(orderEntity);
        var previousEmployeeOrderChanges = orderChanges.stream()
                .filter(it -> !it.getAuthor().equalsIgnoreCase(DEFAULT_SYSTEM_AUTHOR))
                .filter(it -> it.getStatus() != OrderStatus.BRAK)
                .sorted(Comparator.comparing(OrderChangesHistoryEntity::getCreatedAt).reversed())
                .toList();

        var isPilaEmployees = previousEmployeeOrderChanges.getFirst().getWorkplace() == Workplace.PILA1
                || previousEmployeeOrderChanges.getFirst().getWorkplace() == Workplace.PILA2;

        var isPilaEmployeeFoundHisBrak = workplace == Workplace.PILA1 || workplace == Workplace.PILA2;

        // если последнее изменение было сделано двумя распиловщиками
        if (isPilaEmployees && isPilaEmployeeFoundHisBrak) {
            var currentPilaOrderChange = getPilaEmployeeHistory(orderChanges, workplace);
            fineEmployee(currentPilaOrderChange, orderEntity);
        } else if (isPilaEmployees) {
            var pila1OrderChange = getPilaEmployeeHistory(orderChanges, Workplace.PILA1);
            var pila2OrderChange = getPilaEmployeeHistory(orderChanges, Workplace.PILA2);
            fineEmployee(pila1OrderChange, orderEntity);
            fineEmployee(pila2OrderChange, orderEntity);
        } else {
            fineEmployee(previousEmployeeOrderChanges.getFirst(), orderEntity);
        }
    }

    private void fineEmployee(OrderChangesHistoryEntity previousOrderChange, OrderEntity orderEntity) {
        if (previousOrderChange == null) {
            return;
        }
        var previousProcessedAt = previousOrderChange.getCreatedAt().toLocalDate();
        var previousEmployeeUsername = previousOrderChange.getAuthor();
        var previousWorkplace = previousOrderChange.getWorkplace();

        var processedCapacityOptional = employeeCapacityRepository.findByUsernameAndProcessedAtAndWorkplace(previousEmployeeUsername, previousProcessedAt, previousWorkplace);
        var processedCapacity = processedCapacityOptional.orElse(initialProcessedCapacity(previousProcessedAt, previousEmployeeUsername, previousWorkplace));

        var currentFine = calculateCurrentAmount(orderEntity, previousWorkplace);
        var totalEarnedAmount = processedCapacity.getEarnedAmount().subtract(currentFine);

        var currentCapacity = orderEntity.getAreaInMeters();
        var totalCapacity = processedCapacity.getCapacity().subtract(currentCapacity);

        processedCapacity.setEarnedAmount(totalEarnedAmount);
        processedCapacity.setCapacity(totalCapacity);

        processedCapacity.setEarnedAmount(totalEarnedAmount);

        var saved = employeeCapacityRepository.save(processedCapacity);
        saveEmployeeActivityHistory(saved, CapacityOperationType.PENALTY, currentCapacity, currentFine);
    }

    private BigDecimal calculateCurrentAmount(OrderEntity orderEntity, Workplace workplace) {
        var currentCapacity = orderEntity.getAreaInMeters();
        var materialRateCoef = workplaceService.findCoefficientPerMaterialsByWorkplace(workplace);
        var rate = workplaceService.findRateByWorkplace(workplace);
        var resultRate = rate.multiply(materialRateCoef.getOrDefault(orderEntity.getMaterialName(), BigDecimal.ONE));
        return currentCapacity.multiply(resultRate);
    }

    private void saveEmployeeActivityHistory(EmployeeProcessedCapacityEntity employeeProcessedCapacity,
                                             CapacityOperationType operationType,
                                             BigDecimal currentCapacity,
                                             BigDecimal amount) {
        var now = LocalDateTime.now(clock);
        var activityHistory = mapper.toEmployeeActivityHistory(employeeProcessedCapacity, operationType, currentCapacity, amount, now);
        employeeActivityHistoryRepository.save(activityHistory);
    }

    private EmployeeProcessedCapacityEntity initialProcessedCapacity(LocalDate processedAt, String username, Workplace workplace) {
        return new EmployeeProcessedCapacityEntity()
                .setUsername(username)
                .setWorkplace(workplace)
                .setProcessedAt(processedAt)
                .setCapacity(BigDecimal.ZERO)
                .setEarnedAmount(BigDecimal.ZERO);
    }

    private OrderChangesHistoryEntity getPilaEmployeeHistory(List<OrderChangesHistoryEntity> orderHistory, Workplace workplace) {
        return orderHistory.stream()
                .sorted(Comparator.comparing(OrderChangesHistoryEntity::getCreatedAt).reversed())
                .filter(it -> it.getWorkplace() == workplace)
                .findFirst()
                .orElse(null);

    }
}
