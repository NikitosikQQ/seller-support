package ru.seller_support.assignment.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.controller.dto.request.WbSupplyDetails;
import ru.seller_support.assignment.controller.dto.request.order.ImportOrdersRequest;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.DuplicateOrdersFoundException;
import ru.seller_support.assignment.service.ShopService;
import ru.seller_support.assignment.service.comment.CommentService;
import ru.seller_support.assignment.service.order.OrderFilterService;
import ru.seller_support.assignment.service.order.OrderParamsCalculatorService;
import ru.seller_support.assignment.service.order.OrderService;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceImportOrdersProcessor {

    private static final Duration ORDER_DELAY_PERIOD_FROM = Duration.parse("PT24H");
    private static final Duration ORDER_DELAY_PERIOD_TO = Duration.parse("PT48H");

    private final List<MarketplaceAdapter> adapters;

    private final ShopService shopService;
    private final OrderParamsCalculatorService orderParamsCalculatorService;
    private final OrderService orderService;
    private final CommentService commentService;
    private final OrderFilterService orderFilterService;

    public void importNewPostings(ImportOrdersRequest request) {
        List<ShopEntity> shops = shopService.findAll();

        if (Objects.isNull(shops) || shops.isEmpty()) {
            log.warn("Не найдено ни одного магазина, пожалуйста, добавьте магазин");
            return;
        }

        GetPostingsModel getPostingsModel = prepareGetPostingModel(request);

        ExecutorService executor = Executors.newFixedThreadPool(shops.size());
        List<PostingInfoModel> allOrders = getPostingInfoModelByShopAsync(shops, getPostingsModel, executor);
        List<PostingInfoModel> wrongOrders = orderFilterService.filterOrdersByWrong(allOrders, true, false);
        List<PostingInfoModel> correctOrders = orderFilterService.filterOrdersByWrong(allOrders, false, true);

        commentService.addCommentsIfNecessary(correctOrders);
        log.info("Успешно отредактированы комментарии по артикулам");

        executor.shutdown();

        orderParamsCalculatorService.preparePostingResult(correctOrders);
        if (!correctOrders.isEmpty()) {
            try {
                var countOfSaved = orderService.saveAll(correctOrders).size();
                log.info("Успешно получены новые отправления в количестве {}, из них ошибочных артикулов {}", countOfSaved, wrongOrders.size());
            } catch (DataIntegrityViolationException e) {
                log.warn(e.getMessage(), e);
                // на случай редкой гонки и дубликатов на уровне БД
                throw new DuplicateOrdersFoundException();
            }
        }
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
        return orderParamsCalculatorService.sortPostingsByMarketplaceAndColorNumber(postingInfoModels);
    }

    private List<PostingInfoModel> getPostingDataByShop(ShopEntity shop, GetPostingsModel request) {
        try {
            MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
            return adapter.getNewPosting(shop, request);
        } catch (Exception e) {
            log.error("Ошибка при импорте заказов: {}", e.getMessage(), e);
            return Collections.emptyList();
        }

    }

    private MarketplaceAdapter getAdapterByMarketplace(Marketplace marketplace) {
        return adapters.stream()
                .filter(adapter -> adapter.getMarketplace() == marketplace)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        String.format("Нет обработчика маркетплейса %s", marketplace)));
    }

    private GetPostingsModel prepareGetPostingModel(ImportOrdersRequest request) {
        Instant now = Instant.now();
        Instant toOzonDate = now.plus(ORDER_DELAY_PERIOD_TO);

        Instant fromRequested = Optional.ofNullable(request)
                .map(ImportOrdersRequest::getFrom)
                .orElse(null);

        List<WbSupplyDetails> wbSupplyDetails = Optional.ofNullable(request)
                .map(ImportOrdersRequest::getWbSupplyDetails)
                .orElse(null);

        Instant fromOzonDate = Objects.isNull(fromRequested) ? now.minus(ORDER_DELAY_PERIOD_FROM) : fromRequested;

        log.info("Период для импорта заказов: {} - {}", fromOzonDate, toOzonDate);

        return GetPostingsModel.builder()
                .from(fromOzonDate)
                .to(toOzonDate)
                .yandexTo(now)
                .wbSupplies(wbSupplyDetails)
                .build();
    }

}
