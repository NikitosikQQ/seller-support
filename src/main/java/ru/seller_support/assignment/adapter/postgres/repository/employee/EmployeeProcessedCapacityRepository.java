package ru.seller_support.assignment.adapter.postgres.repository.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeProcessedCapacityEntity;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProcessedCapacityRepository extends JpaRepository<EmployeeProcessedCapacityEntity, UUID>,
        JpaSpecificationExecutor<EmployeeProcessedCapacityEntity> {

    Optional<EmployeeProcessedCapacityEntity> findByUsernameAndProcessedAtAndWorkplace(String username,
                                                                                       LocalDate processedAt,
                                                                                       Workplace workplace);

    @Query("""
            SELECT e FROM EmployeeProcessedCapacityEntity e
            WHERE e.processedAt = :today
            ORDER BY e.username
            """)
    List<EmployeeProcessedCapacityEntity> getAllCapacitiesByProcessedAt(LocalDate today);

    @Query("""
            SELECT e FROM EmployeeProcessedCapacityEntity e
            WHERE e.processedAt = :today
                AND e.workplace in :workplaces
            ORDER BY e.username
            """)
    List<EmployeeProcessedCapacityEntity> getActualCapacitiesByWorkplace(LocalDate today, Collection<Workplace> workplaces);

    @Modifying
    @Query("""
                DELETE FROM EmployeeProcessedCapacityEntity o
                WHERE o.processedAt < :maxProcessedAt
            """)
    int cleanUp(LocalDate maxProcessedAt);
}
