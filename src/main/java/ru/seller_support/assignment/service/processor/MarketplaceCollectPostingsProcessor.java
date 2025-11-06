package ru.seller_support.assignment.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.ozon.common.OzonСonstants;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.service.ShopService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceCollectPostingsProcessor {

    private final List<MarketplaceAdapter> adapters;
    private final ShopService shopService;

    public void processCollectPostings() {
        List<ShopEntity> shops = shopService.findAllByMarketplaceActive(Marketplace.OZON);

        if (Objects.isNull(shops) || shops.isEmpty()) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(shops.size());

        GetPostingsModel getPostingsModel = prepareGetPostingModel();

        List<PostingInfoModel> allPostings = getPostingInfoModelByShopAsync(shops, getPostingsModel, executor);
        List<PostingInfoModel> wrongPostings = filterPostingsByWrong(allPostings, true);
        List<PostingInfoModel> correctPostings = filterPostingsByWrong(allPostings, false);

        log.info("Успешно получены отправления на сборку в количестве {}, из них ошибочных артикулов {}", allPostings.size(),
                wrongPostings.size());

        if (correctPostings.isEmpty()) {
            log.info("Нет отправлений, которые требуется собирать на отгрузку");
            return;
        }

        collectPostingsAsync(correctPostings, shops, executor);

        executor.shutdown();
    }

    private List<PostingInfoModel> filterPostingsByWrong(List<PostingInfoModel> postings, boolean needWrong) {
        return postings.stream()
                .filter(post -> needWrong == post.getProduct().getWrongArticle())
                .toList();
    }

    private List<PostingInfoModel> getPostingInfoModelByShopAsync(List<ShopEntity> shops,
                                                                  GetPostingsModel getPostingsRequest,
                                                                  ExecutorService executor) {

        List<PostingInfoModel> postingInfoModels;
        List<CompletableFuture<List<PostingInfoModel>>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        getPostingDataByShop(shop, getPostingsRequest), executor))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<PostingInfoModel>> allResultsFuture = allFutures.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .toList());

        try {
            postingInfoModels = allResultsFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(String.format(
                    "Ошибка при попытке асинхронно запросить отправления по маркетплейсам %s", e.getMessage()), e);
        }
        return postingInfoModels;
    }

    private void collectPostingsAsync(List<PostingInfoModel> postings,
                                      List<ShopEntity> shops,
                                      ExecutorService executor) {

        Map<String, List<PostingInfoModel>> postingsByShop = postings.stream()
                .collect(Collectors.groupingBy(PostingInfoModel::getShopName));

        List<CompletableFuture<Void>> futures = shops.stream()
                .map(shop -> CompletableFuture.runAsync(() -> {
                            try {
                                collectPostingsByShop(shop, postingsByShop.getOrDefault(shop.getName(), Collections.emptyList()));
                            } catch (Exception ex) {
                                log.error("Ошибка при сборе отправлений для магазина {}: {}",
                                        shop.getName(), ex.getMessage(), ex);
                            }
                        }, executor)
                )
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.get();
            log.info("Асинхронный сбор отправлений завершён для магазинов");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Асинхронная сборка отправлений была прервана: {}", e.getMessage(), e);
            throw new RuntimeException("Асинхронная сборка отправлений была прерван", e);
        }
    }

    private void collectPostingsByShop(ShopEntity shop, List<PostingInfoModel> postings) {
        MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
        adapter.collectPostingsAwaitingPackaging(shop, postings);
    }

    private List<PostingInfoModel> getPostingDataByShop(ShopEntity shop, GetPostingsModel request) {
        MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
        return adapter.getNewPosting(shop, request);
    }

    private MarketplaceAdapter getAdapterByMarketplace(Marketplace marketplace) {
        return adapters.stream()
                .filter(adapter -> adapter.getMarketplace() == marketplace)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        String.format("Нет обработчика маркетплейса %s", marketplace)));
    }

    private GetPostingsModel prepareGetPostingModel() {
        Instant toOzonDate = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant fromOzonDate = toOzonDate.minus(8, ChronoUnit.DAYS);

        return GetPostingsModel.builder()
                .from(fromOzonDate)
                .to(toOzonDate)
                .ozonStatus(OzonСonstants.OzonStatus.AWAITING_PACKAGING)
                .build();
    }

}
