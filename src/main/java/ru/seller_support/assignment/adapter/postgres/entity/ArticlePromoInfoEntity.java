package ru.seller_support.assignment.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "article_promo_info")
@Getter
@Setter
public class ArticlePromoInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "material", nullable = false)
    private String material;

    @Column(name = "quantity_per_sku", nullable = false)
    private Integer quantityPerSku;

}
