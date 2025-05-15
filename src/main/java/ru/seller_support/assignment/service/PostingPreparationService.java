package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.domain.SummaryOfMaterialModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingPreparationService {

    private static final BigDecimal MM_TO_METER = BigDecimal.valueOf(1000000);

    private final ArticlePromoInfoService articlePromoInfoService;

    public void preparePostingResult(List<PostingInfoModel> postingInfoModels) {
        log.info("Данные для расчета отчета по отгрузкам: {} ", postingInfoModels);
        List<ArticlePromoInfoEntity> articlePromoInfos = articlePromoInfoService.findAll();
        postingInfoModels.stream()
                .map(PostingInfoModel::getProduct)
                .forEach(product -> {
                    int currentQuantity = product.getQuantity();
                    product.setQuantity(currentQuantity * getRealQuantity(product, articlePromoInfos));
                    product.setAreaInMeters(getAreaInMeter(product));
                    product.setPricePerSquareMeter(getPricePerSquareMeter(product));
                });
    }

    public List<PostingInfoModel> sortPostingsByMarketplaceAndColorNumber(List<PostingInfoModel> postings) {
        return postings.stream()
                .sorted(Comparator.comparing(PostingInfoModel::getMarketplace)
                        .thenComparing(post -> post.getProduct().getColorNumber(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<SummaryOfMaterialModel> calculateSummaryPerDay(List<PostingInfoModel> postings) {
        List<SummaryOfMaterialModel> summary = new ArrayList<>();

        Map<MaterialEntity, List<ArticlePromoInfoEntity>> mapOfMaterials = getMaterialArticlesMap();

        for (Map.Entry<MaterialEntity, List<ArticlePromoInfoEntity>> entry : mapOfMaterials.entrySet()) {
            MaterialEntity material = entry.getKey();
            List<ArticlePromoInfoEntity> articles = entry.getValue();

            List<PostingInfoModel> filteringPosting = getFilteringPostingsByArticle(postings, articles);
            if (filteringPosting.isEmpty()) {
                continue;
            }
            SummaryOfMaterialModel summaryOfMaterial = new SummaryOfMaterialModel();
            BigDecimal totalPricePerDay = calculateTotalPricePerMaterial(filteringPosting);
            BigDecimal totalArea = calculateTotalArea(filteringPosting);

            summaryOfMaterial.setMaterialName(material.getName());
            summaryOfMaterial.setTotalAreaInMeterPerDay(totalArea);
            summaryOfMaterial.setTotalPricePerDay(totalPricePerDay);
            summaryOfMaterial.setAveragePricePerSquareMeter(calculateAveragePricePerMeter(totalPricePerDay, totalArea));

            summary.add(summaryOfMaterial);
        }

        return summary;
    }

    private Integer getRealQuantity(ProductModel product, List<ArticlePromoInfoEntity> articlePromoInfos) {
        return articlePromoInfos.stream()
                .filter(promo -> promo.getName().equalsIgnoreCase(product.getPromoName()))
                .map(ArticlePromoInfoEntity::getQuantityPerSku)
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException(String.format(
                                "Не найдено промо %s, весь артикул %s", product.getPromoName(), product.getArticle())));
    }

    private BigDecimal getAreaInMeter(ProductModel product) {
        BigDecimal length = BigDecimal.valueOf(product.getLength());
        BigDecimal width = BigDecimal.valueOf(product.getWidth());
        BigDecimal quantity = BigDecimal.valueOf(product.getQuantity());

        return length.multiply(width).multiply(quantity).divide(MM_TO_METER, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getPricePerSquareMeter(ProductModel product) {
        try {
            BigDecimal totalPrice = product.getTotalPrice();
            BigDecimal areaInMeters = product.getAreaInMeters();
            return totalPrice.divide(areaInMeters, 0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Ошибка при вычислении цены за квадратный метр для продукта {}, ошибка: {}",
                    product, e.getMessage());
            throw e;
        }
    }

    public Map<MaterialEntity, List<ArticlePromoInfoEntity>> getMaterialArticlesMap() {
        return articlePromoInfoService.findAll()
                .stream()
                .collect(Collectors.groupingBy(ArticlePromoInfoEntity::getMaterial, TreeMap::new, Collectors.toList()));
    }

    private BigDecimal calculateTotalArea(List<PostingInfoModel> materialPostings) {
        return materialPostings.stream()
                .map(PostingInfoModel::getProduct)
                .map(ProductModel::getAreaInMeters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalPricePerMaterial(List<PostingInfoModel> materialPostings) {
        return materialPostings.stream()
                .map(PostingInfoModel::getProduct)
                .map(ProductModel::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAveragePricePerMeter(BigDecimal totalPricePerMaterial,
                                                     BigDecimal totalAreaPerMaterial) {
        return totalPricePerMaterial.divide(totalAreaPerMaterial, 2, RoundingMode.HALF_UP);
    }

    public List<PostingInfoModel> getFilteringPostingsByArticle(List<PostingInfoModel> postings,
                                                                List<ArticlePromoInfoEntity> articlePromoInfos) {
        Set<String> promoNames = extractPromoNames(articlePromoInfos);
        return postings.stream()
                .filter(post -> promoNames.contains(post.getProduct().getPromoName()))
                .toList();
    }

    public Set<String> extractPromoNames(List<ArticlePromoInfoEntity> articles) {
        return articles.stream()
                .map(ArticlePromoInfoEntity::getName)
                .collect(Collectors.toSet());
    }
}
