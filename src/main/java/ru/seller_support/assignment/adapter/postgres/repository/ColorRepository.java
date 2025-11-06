package ru.seller_support.assignment.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.ColorEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ColorRepository extends JpaRepository<ColorEntity, UUID> {

   Optional<ColorEntity> findByNumber(Integer number);

    List<ColorEntity> findAllByOrderByNumberAsc();
}
