package ru.seller_support.assignment.adapter.postgres.entity.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Accessors(chain = true)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false, unique = true)
    private String number;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "pallet_number")
    private Integer palletNumber;

    @Column
    @Enumerated(EnumType.STRING)
    private Marketplace marketplace;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "in_process_at")
    private LocalDateTime inProcessAt;

    @Column(name = "article")
    private String article;

    @Column
    private Integer quantity;

    @Column
    private Integer length;

    @Column
    private Integer width;

    @Column
    private Integer thickness;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "area_in_meters")
    private BigDecimal areaInMeters;

    @Column(name = "price_per_square_meter")
    private BigDecimal pricePerSquareMeter;

    @Column(name = "color_number")
    private Integer colorNumber;

    @Column
    private String color;

    @Column(name = "promo_name")
    private String promoName;

    @Column
    private String comment;

    @Column(name = "material_name")
    private String materialName;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
