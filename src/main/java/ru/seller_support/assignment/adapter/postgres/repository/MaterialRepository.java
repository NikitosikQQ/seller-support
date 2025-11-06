package ru.seller_support.assignment.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID> {

    Optional<MaterialEntity> findByName(String name);

    List<MaterialEntity> findAllByOrderByNameAsc();

    List<MaterialEntity> findAllByUseInChpuTemplate(Boolean useInChpuTemplate);

    List<MaterialEntity> findAllByIdIn(Collection<UUID> ids);

    List<MaterialEntity> findAllByIsOnlyPackaging(Boolean isOnlyPackaging);
}
