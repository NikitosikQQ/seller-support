package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.repository.ArticlePromoInfoRepository;
import ru.seller_support.assignment.controller.dto.request.article.ArticleDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.article.ArticleSaveRequest;
import ru.seller_support.assignment.controller.dto.request.article.ArticleUpdateRequest;
import ru.seller_support.assignment.exception.ArticleChangeException;

import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<ArticlePromoInfoEntity> findAll() {
        List<ArticlePromoInfoEntity> articles = repository.findAllByOrderByTypeAsc();
        return fillChpuMaterialIfExists(articles);
    }

    @Transactional(readOnly = true)
    public List<ArticlePromoInfoEntity> findAllWithComments() {
        return repository.findAllByCommentsIsNotNullOrderByTypeAsc();
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
        if (Objects.nonNull(request.getChpuMaterialName())) {
            MaterialEntity chpuMaterial = materialService.findByName(request.getChpuMaterialName());
            article.setChpuMaterialId(chpuMaterial.getId());
        }

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
        if (Objects.nonNull(request.getChpuMaterialName())) {
            MaterialEntity material = materialService.findByName(request.getChpuMaterialName());
            article.setChpuMaterialId(material.getId());
        } else {
            article.setChpuMaterialId(null);
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

    @Transactional
    public Set<ArticlePromoInfoEntity> findAllByNames(Set<String> articlesName) {
        return repository.findAllByNameIn(articlesName);
    }

    private List<ArticlePromoInfoEntity> fillChpuMaterialIfExists(List<ArticlePromoInfoEntity> articlePromoInfos) {
        Map<UUID, List<ArticlePromoInfoEntity>> articlesWithChpuMaterial = articlePromoInfos.stream()
                .filter(article -> Objects.nonNull(article.getChpuMaterialId()))
                .collect(Collectors.groupingBy(ArticlePromoInfoEntity::getChpuMaterialId));
        List<MaterialEntity> chpuMaterials = materialService.findAllByIds(articlesWithChpuMaterial.keySet());

        for (MaterialEntity material : chpuMaterials) {
            UUID materialId = material.getId();
            List<ArticlePromoInfoEntity> linkedArticles = articlesWithChpuMaterial.get(materialId);
            if (linkedArticles != null) {
                linkedArticles.forEach(article -> article.setChpuMaterial(material));
            }
        }

        return articlePromoInfos;
    }
}
