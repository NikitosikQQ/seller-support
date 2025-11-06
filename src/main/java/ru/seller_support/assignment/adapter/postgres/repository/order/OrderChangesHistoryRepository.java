package ru.seller_support.assignment.adapter.postgres.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderChangesHistoryEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderChangesHistoryRepository extends JpaRepository<OrderChangesHistoryEntity, UUID> {

    List<OrderChangesHistoryEntity> findByOrderId(UUID orderId);

    List<OrderChangesHistoryEntity> findByOrderNumberOrderByCreatedAt(String orderNumber);
}
