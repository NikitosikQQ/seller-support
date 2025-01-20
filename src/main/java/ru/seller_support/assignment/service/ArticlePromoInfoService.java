package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.repository.ArticlePromoInfoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticlePromoInfoService {

    private final ArticlePromoInfoRepository repository;

    public List<ArticlePromoInfoEntity> findAll() {
        return repository.findAll();
    }
}
