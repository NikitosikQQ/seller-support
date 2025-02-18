package ru.seller_support.assignment.adapter.marketplace.wb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.wb.client.WildberriesClient;
import ru.seller_support.assignment.adapter.marketplace.wb.common.WildberriesConstants;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.request.GetStickersRequest;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetOrdersBySupplyIdResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetStickersResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.mapper.WildberriesAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.util.FileUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WildberriesAdapter extends MarketplaceAdapter {

    private static final int MAX_ID_ORDERS_IN_REQUEST = 100;

    private final WildberriesClient client;
    private final WildberriesAdapterMapper mapper;

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.WILDBERRIES;
    }

    @Override
    public List<PostingInfoModel> getNewPosting(ShopEntity shop, GetPostingsModel request) {
        if (!StringUtils.hasLength(request.getSupplyId())) {
            return Collections.emptyList();
        }
        GetOrdersBySupplyIdResponse response = client.getOrdersBySupplyId(shop.getApiKey(), request.getSupplyId());
        log.info("Успешно получены отправления для {} в количестве {}", shop.getName(), response.getOrders().size());

        return response.getOrders()
                .stream()
                .map(order -> {
                    try {
                        return mapper.toPostingInfoModel(order, shop);
                    } catch (ArticleMappingException e) {
                        log.warn(e.getMessage(), e);
                        return mapper.toWrongPostingInfoModel(order, shop);
                    }
                })
                .toList();
    }

    @Override
    public List<byte[]> getPackagesByPostingNumbers(ShopEntity shop, List<PostingInfoModel> postings) {
        List<byte[]> packages = new ArrayList<>();
        Map<String, String> orderIdStickerIdMapping = new HashMap<>();
        Base64.Decoder decoder = Base64.getDecoder();

        List<String> orderIds = postings.stream()
                .map(PostingInfoModel::getPostingNumber)
                .toList();

        for (int i = 0; i < orderIds.size(); i += MAX_ID_ORDERS_IN_REQUEST) {
            List<String> batch = orderIds.subList(i, Math.min(i + MAX_ID_ORDERS_IN_REQUEST, orderIds.size()));

            GetStickersRequest request = buildGetStickersRequest(batch);

            GetStickersResponse partResponse = client.getStickers(shop.getApiKey(),
                    WildberriesConstants.StickerTypes.SVG,
                    WildberriesConstants.StickersSize.MIN_WIDTH,
                    WildberriesConstants.StickersSize.MIN_HEIGHT,
                    request);

            partResponse.getStickers().forEach(sticker -> {
                orderIdStickerIdMapping.put(sticker.getOrderId(), sticker.getPartA().concat(sticker.getPartB()));
                byte[] decodedBytes = decoder.decode(sticker.getFile());
                byte[] pdfBytes = FileUtils.convertSVGtoPDF(decodedBytes);
                packages.add(pdfBytes);
            });
        }
        changeOrderIdToStickerId(postings, orderIdStickerIdMapping);
        log.info("Количество этикеток для магазина {} = {}", shop.getName(), orderIds.size());

        return packages;
    }

    private GetStickersRequest buildGetStickersRequest(List<String> orderIds) {
        return GetStickersRequest.builder()
                .orders(orderIds.stream().map(Long::valueOf).toList())
                .build();
    }

    private void changeOrderIdToStickerId(List<PostingInfoModel> postings,
                                          Map<String, String> orderIdStickerIdMapping) {
        postings.forEach(posting -> {
            String stickerId = orderIdStickerIdMapping.get(posting.getPostingNumber());
            posting.setPostingNumber(stickerId);
        });
    }
}
