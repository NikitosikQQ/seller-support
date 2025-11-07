package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.controller.dto.request.order.SearchOrderRequest;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.service.MaterialService;
import ru.seller_support.assignment.service.ShopService;
import ru.seller_support.assignment.service.mapper.OrderMapper;
import ru.seller_support.assignment.util.FileUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ru.seller_support.assignment.domain.enums.OrderStatus.CREATED;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPackageService {

    private final List<MarketplaceAdapter> adapters;

    private final OrderService orderService;
    private final OrderSearchService orderSearchService;
    private final OrderMapper orderMapper;
    private final MaterialService materialService;
    private final ShopService shopService;

    public byte[] downloadOrderPackages(Boolean onlyPackagesMaterial) {
        var orders = onlyPackagesMaterial
                ? findOrdersForDownloadPackaging(true, List.of(CREATED))
                : findOrdersForDownloadPackaging(false, OrderStatus.STATUSES_FOR_DOWNLOAD_PACKAGES.stream().toList());

        if (CollectionUtils.isEmpty(orders)) {
            return null;
        }

        var models = orders.stream()
                .map(it -> orderMapper.toPostingModel(it, true))
                .toList();
        var shopNames = models.stream()
                .map(PostingInfoModel::getShopName)
                .collect(Collectors.toSet());

        var shops = shopService.findAllByNames(shopNames);
        if (CollectionUtils.isEmpty(shops)) {
            return null;
        }
        var executor = Executors.newFixedThreadPool(shops.size());
        var pdfPackages = getPackagesOfPostingsAsync(shops, models, executor);
        if (CollectionUtils.isEmpty(pdfPackages)) {
            return null;
        }

        return FileUtils.mergePdfFiles(pdfPackages);
    }

    private List<byte[]> getPackagesOfPostingsAsync(List<ShopEntity> shops,
                                                    final List<PostingInfoModel> postingInfoModels,
                                                    ExecutorService executor) {
        List<CompletableFuture<List<byte[]>>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        getPdfBytesPackagesOfPostingsByShop(shop, postingInfoModels), executor))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<byte[]>> allResultsFuture = allFutures.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .toList());

        try {
            return allResultsFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(String.format("Ошибка при попытке асинхронно запросить этикетки по маркетплейсам %s",
                    e.getMessage()), e);
        }
    }

    private List<byte[]> getPdfBytesPackagesOfPostingsByShop(ShopEntity shop, List<PostingInfoModel> postings) {
        try {
            List<PostingInfoModel> actualPostings = postings.stream()
                    .filter(post -> post.getShopName().equalsIgnoreCase(shop.getName()))
                    .toList();
            MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
            return adapter.getPackagesByPostings(shop, actualPostings);
        } catch (Exception e) {
            log.error("Ошибка при попытке скачать этикетки: {}", e.getMessage(), e);
            return Collections.emptyList();
        }

    }

    private MarketplaceAdapter getAdapterByMarketplace(Marketplace marketplace) {
        return adapters.stream()
                .filter(adapter -> adapter.getMarketplace() == marketplace)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        String.format("Нет обработчика маркетплейса %s", marketplace)));
    }

    private List<OrderEntity> findOrdersForDownloadPackaging(boolean isOnlyPackagesMaterials, List<OrderStatus> statuses) {
        var materials = materialService.findAllOnlyPackaging(isOnlyPackagesMaterials);
        var materialNames = materials.stream()
                .map(MaterialEntity::getName)
                .toList();
        if (CollectionUtils.isEmpty(materialNames)) {
            return Collections.emptyList();
        }

        var query = SearchOrderRequest.builder()
                .statuses(statuses)
                .materialNames(materialNames)
                .build();

        return orderSearchService.searchWithoutPage(query);
    }


}
