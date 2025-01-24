package ru.seller_support.assignment.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.util.UUID;

@Entity
@Table(name = "shops")
@Getter
@Setter
public class ShopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "pallet_number", nullable = false)
    private Integer palletNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace", nullable = false)
    private Marketplace marketplace;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "clientId")
    private String clientId;

    @Column(name = "active")
    private Boolean active;

}
