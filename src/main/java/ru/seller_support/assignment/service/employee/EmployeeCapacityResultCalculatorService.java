package ru.seller_support.assignment.service.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeProcessedCapacityEntity;
import ru.seller_support.assignment.domain.EmployeeCapacityResult;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeCapacityResultCalculatorService {

    public List<EmployeeCapacityResult> calculateCapacityResult(List<EmployeeProcessedCapacityEntity> currentData,
                                                                List<EmployeeProcessedCapacityEntity> previousData) {
        Map<UserWorkplaceKey, EmployeeCapacityResult> currentSummary = groupByUsernameAndWorkplace(currentData);
        Map<UserWorkplaceKey, EmployeeCapacityResult> previousSummary = groupByUsernameAndWorkplace(previousData);

        return calculateDeltas(currentSummary, previousSummary);
    }

    private Map<UserWorkplaceKey, EmployeeCapacityResult> groupByUsernameAndWorkplace(List<EmployeeProcessedCapacityEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return Map.of();
        }

        return data.stream()
                .collect(Collectors.groupingBy(e -> new UserWorkplaceKey(e.getUsername(), e.getWorkplace()),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            BigDecimal totalCapacity = sum(list, EmployeeProcessedCapacityEntity::getCapacity);
                            BigDecimal totalEarned = sum(list, EmployeeProcessedCapacityEntity::getEarnedAmount);

                            EmployeeProcessedCapacityEntity first = list.getFirst();
                            return EmployeeCapacityResult.builder()
                                    .username(first.getUsername())
                                    .workplace(first.getWorkplace())
                                    .totalCapacity(totalCapacity)
                                    .totalEarnedAmount(totalEarned)
                                    .build();
                        })
                ));
    }

    private List<EmployeeCapacityResult> calculateDeltas(Map<UserWorkplaceKey, EmployeeCapacityResult> current,
                                                         Map<UserWorkplaceKey, EmployeeCapacityResult> previous) {

        List<EmployeeCapacityResult> results = new ArrayList<>();

        for (var entry : current.entrySet()) {
            var key = entry.getKey();
            var currentResult = entry.getValue();
            var previousResult = previous.getOrDefault(key,
                    EmployeeCapacityResult.builder()
                            .totalCapacity(BigDecimal.ZERO)
                            .totalEarnedAmount(BigDecimal.ZERO)
                            .build()
            );

            BigDecimal deltaCapacity = currentResult.getTotalCapacity()
                    .subtract(previousResult.getTotalCapacity());
            BigDecimal deltaEarned = currentResult.getTotalEarnedAmount()
                    .subtract(previousResult.getTotalEarnedAmount());

            results.add(EmployeeCapacityResult.builder()
                    .username(currentResult.getUsername())
                    .workplace(currentResult.getWorkplace())
                    .totalCapacity(currentResult.getTotalCapacity())
                    .totalEarnedAmount(currentResult.getTotalEarnedAmount())
                    .deltaCapacity(deltaCapacity)
                    .deltaEarnedAmount(deltaEarned)
                    .build());
        }

        return results;
    }

    private <T> BigDecimal sum(Collection<T> data, Function<T, BigDecimal> mapper) {
        return data.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record UserWorkplaceKey(String username, Workplace workplace) {
    }

}
