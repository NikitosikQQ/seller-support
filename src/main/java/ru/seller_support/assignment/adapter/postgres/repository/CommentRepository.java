package ru.seller_support.assignment.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.CommentEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    List<CommentEntity> findAllByOrderByCreatedAtAsc();
}
