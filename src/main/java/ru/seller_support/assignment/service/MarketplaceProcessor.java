package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.PostingGenerationException;
import ru.seller_support.assignment.util.CommonUtils;
import ru.seller_support.assignment.util.FileUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceProcessor {

    private static final String PDF_NAME_PATTERN = "Этикетки %s.pdf";
    private static final String EXCEL_NAME_PATTERN = "Заказы %s.xlsx";

    private final List<MarketplaceAdapter> adapters;

    private final ShopService shopService;
    private final PostingExcelReportGenerator postingExcelReportGenerator;
    private final PostingPreparationService postingPreparationService;

    public byte[] getNewPostings(String from, String to, Instant now) {
        Instant fromDate = CommonUtils.parseStringToInstant(from);
        Instant toDate = CommonUtils.parseStringToInstant(to);

        List<ShopEntity> shops = shopService.findAll();

        ExecutorService executor = Executors.newFixedThreadPool(shops.size());

        List<PostingInfoModel> postings = getPostingInfoModelByShopAsync(shops, fromDate, toDate, executor);
        log.info("Успешно получены отправления в количестве {}", postings.size());

        List<byte[]> pdfRawPackagesBytes = getPackagesOfPostingsAsync(shops, postings, executor);
        log.info("Успешно получены этикетки в количестве {}", postings.size());

        executor.shutdown();

        postingPreparationService.preparePostingResult(postings);

        Map<MaterialEntity,List<ArticlePromoInfoEntity>> articles = postingPreparationService.getMaterialArticlesMap();

        byte[] excelBytes = postingExcelReportGenerator.createNewPostingFile(postings, articles);
        String excelName = CommonUtils.getFormattedStringWithInstant(EXCEL_NAME_PATTERN, now);

        byte[] pdfBytes = FileUtils.mergePdfFiles(pdfRawPackagesBytes);
        String pdfFileName = CommonUtils.getFormattedStringWithInstant(PDF_NAME_PATTERN, now);

        byte[] zip = FileUtils.createZip(excelBytes, excelName, pdfBytes, pdfFileName);
        if (Objects.isNull(zip)) {
            throw new PostingGenerationException(String.format(
                    "Нет отгружаемых отправлений на период с %s по %s", fromDate, toDate));
        }

        return zip;
    }

    private List<PostingInfoModel> getPostingInfoModelByShopAsync(List<ShopEntity> shops,
                                                                  Instant from,
                                                                  Instant to,
                                                                  ExecutorService executor) {
        List<PostingInfoModel> postingInfoModels;
        List<CompletableFuture<List<PostingInfoModel>>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        getPostingDataByShop(shop, from, to), executor))
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
                    "Ошибка при попытке асинхронно запросить отправления по маркетплейсам %s", e.getMessage()));
        }
        return postingPreparationService.sortPostingsByMarketplaceAndColorNumber(postingInfoModels);
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
                    e.getMessage()));
        }
    }

    private List<byte[]> getPdfBytesPackagesOfPostingsByShop(ShopEntity shop, List<PostingInfoModel> postings) {
        List<PostingInfoModel> actualPostings = postings.stream()
                .filter(post -> post.getShopName().equalsIgnoreCase(shop.getName()))
                .toList();
        MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
        return adapter.getPackagesByPostingNumbers(shop, actualPostings);
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



}
