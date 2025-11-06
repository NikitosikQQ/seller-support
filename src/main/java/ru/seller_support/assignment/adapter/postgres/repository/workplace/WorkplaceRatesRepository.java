package ru.seller_support.assignment.adapter.postgres.repository.workplace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceRatesEntity;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.util.UUID;

@Repository
public interface WorkplaceRatesRepository extends JpaRepository<WorkplaceRatesEntity, UUID> {

    WorkplaceRatesEntity findByWorkplace(Workplace workplace);
}
