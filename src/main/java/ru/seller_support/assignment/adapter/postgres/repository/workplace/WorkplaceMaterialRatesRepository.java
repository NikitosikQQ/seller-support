package ru.seller_support.assignment.adapter.postgres.repository.workplace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceMaterialRatesEntity;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkplaceMaterialRatesRepository extends JpaRepository<WorkplaceMaterialRatesEntity, UUID> {

    @Query("SELECT w FROM WorkplaceMaterialRatesEntity w WHERE w.workplace = :workplace")
    List<WorkplaceMaterialRatesEntity> findByWorkplace(Workplace workplace);

    List<WorkplaceMaterialRatesEntity> findAllByWorkplaceAndMaterialName(Workplace workplace, String materialName);
}
