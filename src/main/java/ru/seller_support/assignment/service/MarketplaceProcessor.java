package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.util.CommonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class MarketplaceProcessor {

    private static final BigDecimal MM_TO_METER = BigDecimal.valueOf(1000000);

    private final List<MarketplaceAdapter> adapters;

    private final ShopService shopService;
    private final ExcelService excelService;
    private final ArticlePromoInfoService articlePromoInfoService;

    public void createFile(List<PostingInfoModel> postings) {
        excelService.createReportFile(postings);
    }

    @SneakyThrows
    public List<PostingInfoModel> getNewPostings(String from, String to) {
        Instant fromDate = CommonUtils.parseStringToInstant(from);
        Instant toDate = CommonUtils.parseStringToInstant(to);

        List<ShopEntity> shops = shopService.findAll();

        ExecutorService executor = Executors.newFixedThreadPool(shops.size());

        List<CompletableFuture<List<PostingInfoModel>>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        getPostingDataByShop(shop, fromDate, toDate), executor))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<PostingInfoModel>> allResultsFuture = allFutures.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .toList());

        List<PostingInfoModel> postingInfoModels = allResultsFuture.get();

        executor.shutdown();
        preparePostingResult(postingInfoModels);

        createFile(sortPostingsByColorNumber(postingInfoModels));

        return postingInfoModels;
    }

    private List<PostingInfoModel> getPostingDataByShop(ShopEntity shop, Instant from, Instant to) {
        MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
        return adapter.getNewPosting(shop, from, to);
    }

    private MarketplaceAdapter getAdapterByMarketplace(Marketplace marketplace) {
        return adapters.stream()
                .filter(adapter -> adapter.getMarketplace() == marketplace)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        String.format("Нет обработчика маркетплейса %s", marketplace)));
    }

    private void preparePostingResult(List<PostingInfoModel> postingInfoModels) {
        List<ArticlePromoInfoEntity> articlePromoInfos = articlePromoInfoService.findAll();
        postingInfoModels.stream()
                .map(PostingInfoModel::getProducts)
                .flatMap(List::stream)
                .forEach(product -> {
                    int currentQuantity = product.getQuantity();
                    product.setQuantity(currentQuantity * getRealQuantity(product, articlePromoInfos));
                    product.setAreaInMeters(getAreaInMeter(product));
                    product.setPricePerSquareMeter(getPricePerSquareMeter(product));
                });
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

    public List<PostingInfoModel> sortPostingsByColorNumber(List<PostingInfoModel> postings) {
        return postings.stream()
                .sorted(Comparator.comparing(post -> post.getProducts().getFirst().getColorNumber())) // Сортировка по первому продукту
                .toList();
    }
}
