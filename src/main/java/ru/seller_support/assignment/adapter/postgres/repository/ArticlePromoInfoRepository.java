package ru.seller_support.assignment.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;

import java.util.*;

@Repository
public interface ArticlePromoInfoRepository extends JpaRepository<ArticlePromoInfoEntity, UUID> {

    Optional<ArticlePromoInfoEntity> findByName(String name);

    List<ArticlePromoInfoEntity> findAllByOrderByTypeAsc();

    List<ArticlePromoInfoEntity> findAllByCommentsIsNotNullOrderByTypeAsc();

    Set<ArticlePromoInfoEntity> findAllByNameIn(Collection<String> articlesName);
}
