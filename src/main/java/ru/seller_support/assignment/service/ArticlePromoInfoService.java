package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.repository.ArticlePromoInfoRepository;
import ru.seller_support.assignment.controller.dto.request.ArticleDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.ArticleSaveRequest;
import ru.seller_support.assignment.controller.dto.request.ArticleUpdateRequest;
import ru.seller_support.assignment.exception.ArticleChangeException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ArticlePromoInfoService {

    private final MaterialService materialService;
    private final ArticlePromoInfoRepository repository;

    @Transactional(readOnly = true)
    public ArticlePromoInfoEntity findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Артикул с номером акции/цены %s не найден", name)));
    }

    public List<ArticlePromoInfoEntity> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void save(ArticleSaveRequest request) {
        checkPromoNameInNotUnique(request.getName());
        ArticlePromoInfoEntity article = new ArticlePromoInfoEntity();

        MaterialEntity material = materialService.findByName(request.getMaterialName());

        article.setMaterial(material);
        article.setName(request.getName());
        article.setType(request.getType());
        article.setQuantityPerSku(request.getQuantityPerSku());

        repository.save(article);
    }

    @Transactional
    public void update(ArticleUpdateRequest request) {
        checkPromoNameInNotUnique(request.getUpdatedName());
        ArticlePromoInfoEntity article = findByName(request.getCurrentName());
        if (Objects.nonNull(request.getMaterialName())) {
            MaterialEntity material = materialService.findByName(request.getMaterialName());
            article.setMaterial(material);
        }
        if (Objects.nonNull(request.getUpdatedName())) {
            article.setName(request.getUpdatedName());
        }
        if (Objects.nonNull(request.getType())) {
            article.setType(request.getType());
        }
        if (Objects.nonNull(request.getQuantityPerSku())) {
            article.setQuantityPerSku(request.getQuantityPerSku());
        }
        repository.save(article);
    }

    private void checkPromoNameInNotUnique(String promoName) {
        if (Objects.isNull(promoName)) {
            return;
        }
        if (repository.findByName(promoName).isPresent()) {
            throw new ArticleChangeException(String.format(
                    "Номер акции/цены %s для артикулов уже существует", promoName));
        }
    }

    @Transactional
    public void delete(ArticleDeleteRequest request) {
        ArticlePromoInfoEntity article = findByName(request.getName());
        repository.delete(article);
    }
}
