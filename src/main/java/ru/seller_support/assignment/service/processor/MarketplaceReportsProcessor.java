package ru.seller_support.assignment.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeProcessedCapacityEntity;
import ru.seller_support.assignment.controller.dto.FileDto;
import ru.seller_support.assignment.controller.dto.request.order.SearchOrderRequest;
import ru.seller_support.assignment.controller.dto.request.report.EmployeeCapacitySearchRequest;
import ru.seller_support.assignment.controller.dto.request.report.GeneratePostingsReportRequest;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.PostingGenerationException;
import ru.seller_support.assignment.service.OrderParamsCalculatorService;
import ru.seller_support.assignment.service.ShopService;
import ru.seller_support.assignment.service.comment.CommentService;
import ru.seller_support.assignment.service.employee.EmployeeCapacityResultCalculatorService;
import ru.seller_support.assignment.service.employee.EmployeeCapacitySearchService;
import ru.seller_support.assignment.service.mapper.OrderMapper;
import ru.seller_support.assignment.service.order.OrderSearchService;
import ru.seller_support.assignment.service.order.OrderService;
import ru.seller_support.assignment.service.report.ChpuTemplateExcelGenerator;
import ru.seller_support.assignment.service.report.EmployeeCapacityResultReportGenerator;
import ru.seller_support.assignment.service.report.PostingExcelReportGenerator;
import ru.seller_support.assignment.util.CommonUtils;
import ru.seller_support.assignment.util.FileUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceReportsProcessor {

    private static final String PDF_NAME_PATTERN = "Этикетки %s.pdf";
    private static final String EXCEL_NAME_PATTERN = "Заказы %s.xlsx";
    private static final String ORDERS_REPORT_NAME = "Заказы.xlsx";

    private static final String EMPLOYEE_CAPACITY_REPORT_NAME = "Итог_выполненных_работ.xlsx";

    private final List<MarketplaceAdapter> adapters;

    private final ShopService shopService;

    private final CommentService commentService;

    private final OrderService orderService;
    private final OrderSearchService orderSearchService;
    private final OrderParamsCalculatorService orderParamsCalculatorService;

    private final EmployeeCapacitySearchService employeeCapacitySearchService;
    private final EmployeeCapacityResultCalculatorService employeeCapacityResultCalculatorService;

    private final ChpuTemplateExcelGenerator chpuTemplateExcelGenerator;
    private final PostingExcelReportGenerator postingExcelReportGenerator;
    private final EmployeeCapacityResultReportGenerator employeeCapacityResultReportGenerator;

    private final OrderMapper orderMapper;

    public byte[] getNewPostingsAndGenerateReport(GeneratePostingsReportRequest request) {
        Instant now = Instant.now();
        List<ShopEntity> shops = shopService.findAll();

        if (Objects.isNull(shops) || shops.isEmpty()) {
            throw new IllegalArgumentException("Не найдено ни одного магазина, пожалуйста, добавьте магазин");
        }
        ExecutorService executor = Executors.newFixedThreadPool(shops.size());

        GetPostingsModel getPostingsModel = prepareGetPostingModel(request);

        List<PostingInfoModel> allPostings = getPostingInfoModelByShopAsync(shops, getPostingsModel, executor);
        List<PostingInfoModel> filteringPostings = orderParamsCalculatorService.excludeOzonPostingsByPeriod(allPostings, request.getExcludeFromOzon(), request.getExcludeToOzon());
        List<PostingInfoModel> wrongPostings = filterPostingsByWrong(filteringPostings, true, false);
        List<PostingInfoModel> wrongBoxPostings = filterPostingsByWrongBox(filteringPostings);
        List<PostingInfoModel> correctPostings = filterPostingsByWrong(filteringPostings, false, true);

        log.info("Успешно получены отправления в количестве {}, из них ошибочных артикулов {}", filteringPostings.size(),
                wrongPostings.size());

        commentService.addCommentsIfNecessary(correctPostings);
        log.info("Успешно отредактированы комментарии по артикулам");

        List<byte[]> pdfRawPackagesBytes = getPackagesOfPostingsAsync(shops, filteringPostings, executor);
        log.info("Успешно получены этикетки в количестве {}", filteringPostings.size());

        executor.shutdown();

        orderParamsCalculatorService.preparePostingResult(correctPostings);

        byte[] excelBytes = postingExcelReportGenerator.createNewOrdersReportFile(correctPostings, wrongPostings, wrongBoxPostings);
        String excelName = CommonUtils.getFormattedStringWithInstant(EXCEL_NAME_PATTERN, now);

        Map<String, byte[]> chpuTemplates = chpuTemplateExcelGenerator.createChpuTemplates(correctPostings);

        byte[] pdfBytes = FileUtils.mergePdfFiles(pdfRawPackagesBytes);
        String pdfFileName = CommonUtils.getFormattedStringWithInstant(PDF_NAME_PATTERN, now);

        byte[] zip = FileUtils.createZip(excelBytes, excelName, pdfBytes, pdfFileName, chpuTemplates);
        if (Objects.isNull(zip)) {
            throw new PostingGenerationException(String.format(
                    "Нет отгружаемых отправлений на период с %s по %s",
                    getPostingsModel.getFrom(), getPostingsModel.getTo()));
        }

        return zip;
    }

    public FileDto generateChpuTemplateReport(List<String> orderNumbers) {
        var orders = orderService.findByNumbersIn(orderNumbers);
        var models = orders.stream()
                .map(it -> orderMapper.toPostingModel(it, false))
                .toList();
        var bytesMap = chpuTemplateExcelGenerator.createChpuTemplates(models);
        var fileName = bytesMap.keySet().stream().toList().getFirst();
        var bytes = bytesMap.values().stream().toList().getFirst();
        return FileDto.builder()
                .fileName(fileName)
                .content(bytes)
                .build();
    }

    public FileDto generateEmployeeCapacityOrder(EmployeeCapacitySearchRequest request) {
        var foundedData = employeeCapacitySearchService.search(request);
        if (CollectionUtils.isEmpty(foundedData)) {
            return null;
        }
        var minProcessAt = request.getFrom();
        var maxProcessAt = request.getTo();

        if (minProcessAt == null) {
            minProcessAt = foundedData.stream()
                    .map(EmployeeProcessedCapacityEntity::getProcessedAt)
                    .min(LocalDate::compareTo)
                    .orElse(null);
        }
        if (maxProcessAt == null) {
            maxProcessAt = foundedData.stream()
                    .map(EmployeeProcessedCapacityEntity::getProcessedAt)
                    .max(LocalDate::compareTo)
                    .orElse(null);
        }


        long daysBetween = ChronoUnit.DAYS.between(minProcessAt, maxProcessAt) + 1;

        var previousMinProcessedAt = minProcessAt.minusDays(daysBetween);
        var previousMaxProcessedAt = maxProcessAt.minusDays(daysBetween);

        var previousData = employeeCapacitySearchService.search(request.toBuilder()
                .from(previousMinProcessedAt)
                .to(previousMaxProcessedAt)
                .build());


        var results = employeeCapacityResultCalculatorService.calculateCapacityResult(foundedData, previousData);
        var bytes = employeeCapacityResultReportGenerator.generateReport(results, minProcessAt, maxProcessAt);

        return FileDto.builder()
                .fileName(EMPLOYEE_CAPACITY_REPORT_NAME)
                .content(bytes)
                .build();
    }

    public FileDto searchOrderAndGenerateReport(SearchOrderRequest query) {
        var orders = orderSearchService.searchWithoutPage(query);
        if (CollectionUtils.isEmpty(orders)) {
            return null;
        }
        var models = orders.stream()
                .map(it -> orderMapper.toPostingModel(it, false))
                .toList();
        var bytes = postingExcelReportGenerator.createNewOrdersReportFile(models, List.of(), List.of());
        return FileDto.builder()
                .fileName(ORDERS_REPORT_NAME)
                .content(bytes)
                .build();
    }

    private List<PostingInfoModel> filterPostingsByWrongBox(List<PostingInfoModel> postings) {
        return postings.stream()
                .filter(post -> post.getProduct().getWrongBox().equals(Boolean.TRUE))
                .toList();
    }

    private List<PostingInfoModel> filterPostingsByWrong(List<PostingInfoModel> postings, boolean needWrong, boolean filterWrongBox) {
        Stream<PostingInfoModel> stream = postings.stream().filter(post -> needWrong == post.getProduct().getWrongArticle());
        return filterWrongBox
                ? stream.filter(post -> post.getProduct().getWrongBox().equals(Boolean.FALSE)).toList()
                : stream.toList();
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
        List<PostingInfoModel> actualPostings = postings.stream()
                .filter(post -> post.getShopName().equalsIgnoreCase(shop.getName()))
                .toList();
        MarketplaceAdapter adapter = getAdapterByMarketplace(shop.getMarketplace());
        return adapter.getPackagesByPostings(shop, actualPostings);
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

    private GetPostingsModel prepareGetPostingModel(GeneratePostingsReportRequest request) {

        Instant fromOzonDate = CommonUtils.parseStringToInstantOzon(request.getFrom(), true);
        Instant toOzonDate = CommonUtils.parseStringToInstantOzon(request.getTo(), false);
        Instant toYandexDate = CommonUtils.parseStringToInstant(request.getYandexTo());

        return GetPostingsModel.builder()
                .from(fromOzonDate)
                .to(toOzonDate)
                .yandexTo(toYandexDate)
                .wbSupplies(request.getSupplies())
                .build();
    }

}
