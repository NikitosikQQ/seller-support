package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.domain.*;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.service.ArticlePromoInfoService;
import ru.seller_support.assignment.util.CommonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderParamsCalculatorService {

    private static final BigDecimal MM_TO_METER = BigDecimal.valueOf(1000000);

    private final ArticlePromoInfoService articlePromoInfoService;

    public void preparePostingResult(List<PostingInfoModel> postingInfoModels) {
        List<ArticlePromoInfoEntity> articlePromoInfos = articlePromoInfoService.findAll();
        Map<String, String> promoNameMaterialNameMap = getPromoNameMaterialNameMap(articlePromoInfos);
        postingInfoModels.stream()
                .map(PostingInfoModel::getProduct)
                .forEach(product -> {
                    int currentQuantity = product.getQuantity();
                    product.setQuantity(currentQuantity * getRealQuantity(product, articlePromoInfos));
                    product.setAreaInMeters(getAreaInMeter(product));
                    product.setPricePerSquareMeter(getPricePerSquareMeter(product));
                    product.setMaterialName(promoNameMaterialNameMap.get(product.getPromoName()));
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

    public List<AveragePricePerMeterLdsp16Model> calculateAveragePriceLdsp16PerMeter(List<PostingInfoModel> postings) {
        List<AveragePricePerMeterLdsp16Model> averagePricePerMeterLdsp16 = new ArrayList<>();

        List<PostingInfoModel> ldsp16PostingsOzon = postings.stream()
                .filter(it -> it.getMarketplace().equals(Marketplace.OZON))
                .filter(it -> it.getProduct().getMaterialName().equalsIgnoreCase("ЛДСП"))
                .toList();
        List<PostingInfoModel> ldsp16PostingsWB = postings.stream()
                .filter(it -> it.getMarketplace().equals(Marketplace.WILDBERRIES))
                .filter(it -> it.getProduct().getMaterialName().equalsIgnoreCase("ЛДСП"))
                .toList();

        if (!ldsp16PostingsOzon.isEmpty()) {
            BigDecimal totalPricePerDayOzon = calculateTotalPricePerMaterial(ldsp16PostingsOzon);
            BigDecimal totalAreaOzon = calculateTotalArea(ldsp16PostingsOzon);
            BigDecimal averagePricePerMeterOzon = calculateAveragePricePerMeter(totalPricePerDayOzon, totalAreaOzon);
            AveragePricePerMeterLdsp16Model ozonAverage = new AveragePricePerMeterLdsp16Model(averagePricePerMeterOzon, Marketplace.OZON);
            averagePricePerMeterLdsp16.add(ozonAverage);
        }

        if (!ldsp16PostingsWB.isEmpty()) {
            BigDecimal totalPricePerDayWb = calculateTotalPricePerMaterial(ldsp16PostingsWB);
            BigDecimal totalAreaWb = calculateTotalArea(ldsp16PostingsWB);
            BigDecimal averagePricePerMeterWb = calculateAveragePricePerMeter(totalPricePerDayWb, totalAreaWb);
            AveragePricePerMeterLdsp16Model wbAverage = new AveragePricePerMeterLdsp16Model(averagePricePerMeterWb, Marketplace.WILDBERRIES);
            averagePricePerMeterLdsp16.add(wbAverage);
        }

        return averagePricePerMeterLdsp16;
    }

    public List<PostingInfoModel> excludeOzonPostingsByPeriod(List<PostingInfoModel> postings, Instant from, Instant to) {
        if (Objects.isNull(from) || Objects.isNull(to)) {
            return postings;
        }

        LocalDateTime excludeFromMsk = CommonUtils.toMoscowLocalDateTime(from);
        LocalDateTime excludeToMsk = CommonUtils.toMoscowLocalDateTime(to);

        return postings.stream()
                .filter(posting -> {
                    if (posting.getMarketplace() != Marketplace.OZON) {
                        return true;
                    }
                    LocalDateTime inProcessAt = posting.getInProcessAt();
                    return inProcessAt.isBefore(excludeFromMsk) || inProcessAt.isAfter(excludeToMsk);
                })
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

    public Map<String, String> getPromoNameMaterialNameMap(List<ArticlePromoInfoEntity> articles) {
        return articles.stream()
                .collect(Collectors.toMap(
                        ArticlePromoInfoEntity::getName,
                        it -> it.getMaterial().getName()));
    }

    public Map<MaterialEntity, List<ArticlePromoInfoEntity>> getChpuMaterialArticlesMap() {
        return articlePromoInfoService.findAll().stream()
                .filter(article -> Objects.isNull(article.getChpuMaterial())
                        ? article.getMaterial().getUseInChpuTemplate()
                        : article.getChpuMaterial().getUseInChpuTemplate())
                .collect(Collectors.groupingBy(
                        article -> Objects.isNull(article.getChpuMaterial()) ? article.getMaterial() : article.getChpuMaterial(),
                        TreeMap::new,
                        Collectors.toList()));
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

    public Map<Integer, List<PostingInfoModel>> groupByThicknessAndArticles(List<PostingInfoModel> postings,
                                                                            List<ArticlePromoInfoEntity> articlePromoInfos) {
        Set<String> promoNames = extractPromoNames(articlePromoInfos);
        return postings.stream()
                .filter(post -> promoNames.contains(post.getProduct().getPromoName()))
                .collect(Collectors.groupingBy(it -> it.getProduct().getThickness()));

    }

    public InProcessAtPeriod calculateOrdersPeriod(List<PostingInfoModel> orders) {
        var fromInProcessAt = orders.stream()
                .map(PostingInfoModel::getInProcessAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        var toInProcessAt = orders.stream()
                .map(PostingInfoModel::getInProcessAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return InProcessAtPeriod.builder()
                .fromInProcessAt(fromInProcessAt)
                .toInProcessAt(toInProcessAt)
                .build();
    }
}
