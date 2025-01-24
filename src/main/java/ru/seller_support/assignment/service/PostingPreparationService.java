package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingPreparationService {

    private static final BigDecimal MM_TO_METER = BigDecimal.valueOf(1000000);

    private final ArticlePromoInfoService articlePromoInfoService;

    public void preparePostingResult(List<PostingInfoModel> postingInfoModels) {
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
                        .thenComparing(post -> post.getProduct().getColorNumber()))
                .toList();
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
        BigDecimal totalPrice = product.getTotalPrice();
        BigDecimal areaInMeters = product.getAreaInMeters();
        return totalPrice.divide(areaInMeters, 0, RoundingMode.HALF_UP);
    }

    public Map<MaterialEntity, List<ArticlePromoInfoEntity>> getMaterialArticlesMap() {
        return articlePromoInfoService.findAll()
                .stream()
                .collect(Collectors.groupingBy(ArticlePromoInfoEntity::getMaterial, TreeMap::new, Collectors.toList()));
    }
}
