package ru.seller_support.assignment.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, UUID> {

    List<ShopEntity> findAllByActive(boolean active);
}
