package ru.seller_support.assignment.adapter.marketplace.ozon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.ozon.client.OzonClient;
import ru.seller_support.assignment.adapter.marketplace.ozon.common.OzonСonstants;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.FilterBody;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetPackagesRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetUnfulfilledListRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.response.GetUnfulfilledListResponse;
import ru.seller_support.assignment.adapter.marketplace.ozon.mapper.OzonAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OzonAdapter extends MarketplaceAdapter {

    private static final int MAX_POSTING_NUMBERS_IN_REQUEST = 20;

    private final OzonClient ozonClient;
    private final OzonAdapterMapper mapper;

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.OZON;
    }

    @Override
    public List<PostingInfoModel> getNewPosting(ShopEntity shop, Instant from, Instant to) {
        GetUnfulfilledListRequest request = buildGetPostingRequest(from, to);
        GetUnfulfilledListResponse response = ozonClient.getUnfulfilledOrders(shop.getApiKey(), shop.getClientId(), request);

        return response.getResult().getPostings().stream()
                .map(post -> mapper.toPostingInfoModel(post, shop))
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

            byte[] partPackagesBytes = ozonClient.getPackages(shop.getApiKey(), shop.getClientId(), request);
            packages.add(partPackagesBytes);
        }
        log.info("Количество этикеток для магазина {} = {}", shop.getName(), postingNumbers.size());

        return packages;
    }

    private GetUnfulfilledListRequest buildGetPostingRequest(Instant from, Instant to) {
        return GetUnfulfilledListRequest.builder()
                .dir(OzonСonstants.ASC_SORT)
                .filter(FilterBody.builder()
                        .cutoffFrom(from)
                        .cutoffTo(to)
                        .status(OzonСonstants.OzonStatus.AWAITING_DELIVER)
                        .build())
                .limit(OzonСonstants.MAX_LIMIT)
                .build();
    }

    private GetPackagesRequest buildGetPackagesRequest(List<String> postingNumbers) {
        return GetPackagesRequest.builder()
                .postingNumber(postingNumbers)
                .build();
    }
}
