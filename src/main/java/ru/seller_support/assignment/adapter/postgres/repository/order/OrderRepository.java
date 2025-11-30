package ru.seller_support.assignment.adapter.postgres.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    @Query("""
                SELECT o.number
                FROM OrderEntity o
                WHERE o.status IN :statuses
                AND o.marketplace IN :marketplaces
                  AND NOT EXISTS (
                        SELECT 1
                        FROM OrderChangesHistoryEntity h
                        WHERE h.orderId = o.id
                          AND h.createdAt >= :threshold
                  )
                  ORDER BY o.inProcessAt
            """)
    List<String> findOrderNumbersForRefresh(@Param("statuses") Collection<OrderStatus> statuses,
                                            @Param("threshold") LocalDateTime threshold,
                                            @Param("marketplaces") Collection<Marketplace> marketplaces);

    OrderEntity findByNumber(String number);

    List<OrderEntity> findAllByExternalOrderNumber(String externalOrderNumber);

    List<OrderEntity> findByNumberIn(Collection<String> numbers);

    @Query("""
                SELECT o.number
                FROM OrderEntity o
                WHERE o.number IN :numbers
            """)
    List<String> findAllNumbersByOrderNumberIn(List<String> numbers);

    @Modifying
    @Query("""
                DELETE FROM OrderEntity o
                WHERE o.createdAt < :maxCreatedAt
                  AND o.status IN :finalStatuses
            """)
    int cleanUp(LocalDateTime maxCreatedAt, Set<OrderStatus> finalStatuses);

    List<OrderEntity> findByStatusIn(Collection<OrderStatus> statuses);
}
