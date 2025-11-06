package ru.seller_support.assignment.adapter.postgres.repository.employee;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeActivityHistoryEntity;
import ru.seller_support.assignment.controller.dto.response.EmployeeCapacityDto;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeActivityHistoryRepository extends JpaRepository<EmployeeActivityHistoryEntity, UUID> {

    @Query("""
                SELECT new ru.seller_support.assignment.controller.dto.response.EmployeeCapacityDto(
                    e.username,
                    e.workplace,
                    SUM(
                        CASE WHEN e.operationType = 'EARNING'
                             THEN e.capacity
                             ELSE -e.capacity
                        END
                    ),
                    SUM(
                        CASE WHEN e.operationType = 'EARNING'
                             THEN e.amount
                             ELSE -e.amount
                        END
                    )
                )
                FROM EmployeeActivityHistoryEntity e
                WHERE e.processedAt = :today
                  AND e.workplace IN :workplaces
                  AND e.createdAt <= :timeLimit
                GROUP BY e.username, e.workplace
            """)
    List<EmployeeCapacityDto> getCapacitiesByWorkplacesWithDelay(
            @Param("today") LocalDate today,
            @Param("workplaces") Collection<Workplace> workplaces,
            @Param("timeLimit") LocalDateTime timeLimit
    );

    @Modifying
    @Query("""
                DELETE FROM EmployeeActivityHistoryEntity o
                WHERE o.processedAt < :maxProcessedAt
            """)
    int cleanUp(LocalDate maxProcessedAt);
}
