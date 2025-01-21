package ru.seller_support.assignment.adapter.marketplace.ozon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.ozon.client.OzonClient;
import ru.seller_support.assignment.adapter.marketplace.ozon.common.OzonСonstants;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.FilterBody;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetUnfulfilledListRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.response.GetUnfulfilledListResponse;
import ru.seller_support.assignment.adapter.marketplace.ozon.mapper.OzonAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OzonAdapter extends MarketplaceAdapter {

    private final OzonClient ozonClient;
    private final OzonAdapterMapper mapper;

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.OZON;
    }

    @Override
    public List<PostingInfoModel> getNewPosting(ShopEntity shop, Instant from, Instant to) {
        GetUnfulfilledListRequest request = buildRequest(from, to);
        GetUnfulfilledListResponse response = ozonClient.getUnfulfilledOrders(shop.getApiKey(), shop.getClientId(), request);

        return response.getResult().getPostings().stream()
                .map(post -> mapper.toPostingInfoModel(post, shop.getPalletNumber(), shop.getName()))
                .toList();
    }

    private GetUnfulfilledListRequest buildRequest(Instant from, Instant to) {
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
}
