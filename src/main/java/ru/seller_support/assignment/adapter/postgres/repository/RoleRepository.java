package ru.seller_support.assignment.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.RoleEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    List<RoleEntity> findByNameIn(Collection<String> roleNames);
}
