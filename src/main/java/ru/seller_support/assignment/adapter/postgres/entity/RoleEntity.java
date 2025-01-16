package ru.seller_support.assignment.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

}
