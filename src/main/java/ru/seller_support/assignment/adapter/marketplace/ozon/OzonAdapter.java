package ru.seller_support.assignment.adapter.marketplace.ozon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.ozon.client.OzonClient;
import ru.seller_support.assignment.adapter.marketplace.ozon.common.OzonСonstants;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.FilterBody;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.Posting;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.CollectPostingsRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetPackagesRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetUnfulfilledListRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.response.CollectPostingsResponse;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.response.GetUnfulfilledListResponse;
import ru.seller_support.assignment.adapter.marketplace.ozon.mapper.OzonAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.service.TextEncryptService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OzonAdapter extends MarketplaceAdapter {

    private static final int MAX_POSTING_NUMBERS_IN_REQUEST = 20;

    private final OzonClient ozonClient;
    private final OzonAdapterMapper mapper;
    private final TextEncryptService encryptService;
    private final OzonPostingSplitter postingSplitter;

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.OZON;
    }

    @Override
    public List<PostingInfoModel> getNewPosting(ShopEntity shop, GetPostingsModel request) {
        List<GetUnfulfilledListResponse> responses = new ArrayList<>();
        if (Objects.isNull(request.getOzonStatus())) {
            GetUnfulfilledListRequest requestAwaitingDeliver = buildGetPostingRequest(request, OzonСonstants.OzonStatus.AWAITING_DELIVER);
            GetUnfulfilledListRequest requestAcceptanceInProgress = buildGetPostingRequest(request, OzonСonstants.OzonStatus.ACCEPTANCE_IN_PROGRESS);
            GetUnfulfilledListResponse responseAwaitingDeliver = ozonClient.getUnfulfilledOrders(
                    encryptService.decrypt(shop.getApiKey()), shop.getClientId(), requestAwaitingDeliver);
            responses.add(responseAwaitingDeliver);

            GetUnfulfilledListResponse responseAcceptanceInProgress = ozonClient.getUnfulfilledOrders(
                    encryptService.decrypt(shop.getApiKey()), shop.getClientId(), requestAcceptanceInProgress);
            responses.add(responseAcceptanceInProgress);
        } else {
            GetUnfulfilledListRequest requestWithStatus = buildGetPostingRequest(request, request.getOzonStatus());
            GetUnfulfilledListResponse response = ozonClient.getUnfulfilledOrders(
                    encryptService.decrypt(shop.getApiKey()), shop.getClientId(), requestWithStatus);
            responses.add(response);
        }

        List<Posting> postingOriginalResponse = responses.stream()
                .map(response -> response.getResult().getPostings())
                .flatMap(List::stream)
                .toList();

        log.info("Успешно получены отправления для {} в количестве {}",
                shop.getName(), postingOriginalResponse.size());

        List<Posting> splittedPostings = postingSplitter.splitOrders(postingOriginalResponse);

        return splittedPostings.stream()
                .map(post -> {
                    try {
                        return mapper.toPostingInfoModel(post, shop);
                    } catch (ArticleMappingException e) {
                        log.warn(e.getMessage(), e);
                        return mapper.toWrongPostingInfoModel(post, shop);
                    }
                })
                .toList();
    }

    @Override
    public List<byte[]> getPackagesByPostingNumbers(ShopEntity shop, List<PostingInfoModel> postings) {
        List<byte[]> packages = new ArrayList<>();

        List<String> postingNumbers = postings.stream()
                .map(PostingInfoModel::getPostingNumber)
                .toList();

        for (int i = 0; i < postingNumbers.size(); i += MAX_POSTING_NUMBERS_IN_REQUEST) {
            List<String> batch = postingNumbers.subList(i, Math.min(i + MAX_POSTING_NUMBERS_IN_REQUEST, postingNumbers.size()));

            GetPackagesRequest request = buildGetPackagesRequest(batch);

            byte[] partPackagesBytes = ozonClient.getPackages(encryptService.decrypt(shop.getApiKey()), shop.getClientId(), request);
            packages.add(partPackagesBytes);
        }
        log.info("Количество этикеток для магазина {} = {}", shop.getName(), postingNumbers.size());

        return packages;
    }

    @Override
    public void collectPostingsAwaitingPackaging(ShopEntity shop, List<PostingInfoModel> postings) {
        try {
            if (postings.isEmpty()) {
                return;
            }

            Map<String, List<ProductModel>> groupedProducts = postings.stream()
                    .collect(Collectors.groupingBy(PostingInfoModel::getPostingNumber,
                            Collectors.mapping(PostingInfoModel::getProduct, Collectors.toList())));
            List<String> collectionResultPostingNumbers = new ArrayList<>();

            groupedProducts.forEach((postingNumber, products) -> {
                CollectPostingsRequest request = new CollectPostingsRequest();
                request.setPostingNumber(postingNumber);
                List<CollectPostingsRequest.Package> packages = new ArrayList<>();
                products.forEach(product -> {
                    Integer currentQuantity = product.getQuantity();
                    while (currentQuantity != 0) {
                        List<CollectPostingsRequest.Product> productInRequest = prepareProductsWithOneSku(product);
                        CollectPostingsRequest.Package requestPackage = CollectPostingsRequest.Package.builder()
                                .products(productInRequest)
                                .build();
                        packages.add(requestPackage);
                        currentQuantity--;
                    }
                });
                request.setPackages(packages);
                CollectPostingsResponse response = ozonClient.collectPostings(encryptService.decrypt(shop.getApiKey()), shop.getClientId(), request);
                collectionResultPostingNumbers.addAll(response.getResult());
            });

            log.info("Успешно собрано из {} изначальных отправлений для {} новых отправлений для магазина {}, ",
                    postings.size(), collectionResultPostingNumbers.size(), shop.getName());
        } catch (Exception e) {
            log.error("Возникла ошибка при попытке собрать заказы для магазина {}: {}", shop.getName(), e.getMessage(), e);
        }
    }

    private GetUnfulfilledListRequest buildGetPostingRequest(GetPostingsModel request, String ozonStatus) {
        return GetUnfulfilledListRequest.builder()
                .dir(OzonСonstants.ASC_SORT)
                .filter(FilterBody.builder()
                        .cutoffFrom(request.getFrom())
                        .cutoffTo(request.getTo())
                        .status(ozonStatus)
                        .build())
                .limit(OzonСonstants.MAX_LIMIT)
                .build();
    }

    private GetPackagesRequest buildGetPackagesRequest(List<String> postingNumbers) {
        return GetPackagesRequest.builder()
                .postingNumber(postingNumbers)
                .build();
    }

    private List<CollectPostingsRequest.Product> prepareProductsWithOneSku(ProductModel product) {
        CollectPostingsRequest.Product collectionProduct = CollectPostingsRequest.Product.builder()
                .productId(Long.parseLong(product.getMarketplaceProductId()))
                .quantity(1)
                .build();
        return List.of(collectionProduct);
    }
}
