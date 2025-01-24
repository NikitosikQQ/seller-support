package ru.seller_support.assignment.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.seller_support.assignment.domain.enums.SortingPostingByParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "material")
@Getter
@Setter
public class MaterialEntity implements Comparable<MaterialEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "separator_name")
    private String separatorName;

    @OneToMany(mappedBy = "material")
    private List<ArticlePromoInfoEntity> articlePromoInfos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "sorting_posting_by")
    private SortingPostingByParam sortingPostingBy;

    public boolean isNotSeparate() {
        return Objects.isNull(separatorName);
    }

    @Override
    public int compareTo(MaterialEntity other) {
        if (Objects.isNull(other)) {
            return 1;
        }
        return this.name.compareTo(other.name);
    }
}
