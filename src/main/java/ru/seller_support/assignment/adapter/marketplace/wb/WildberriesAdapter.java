package ru.seller_support.assignment.adapter.marketplace.wb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.wb.client.WildberriesClient;
import ru.seller_support.assignment.adapter.marketplace.wb.common.WildberriesConstants;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.request.GetStickersRequest;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetOrdersBySupplyIdResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetStickersResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.mapper.WildberriesAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.controller.dto.request.WbSupplyDetails;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.service.TextEncryptService;
import ru.seller_support.assignment.util.FileUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WildberriesAdapter extends MarketplaceAdapter {

    private static final int MAX_ID_ORDERS_IN_REQUEST = 100;

    private final WildberriesClient client;
    private final WildberriesAdapterMapper mapper;
    private final TextEncryptService encryptService;

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.WILDBERRIES;
    }

    @Override
    public List<PostingInfoModel> getNewPosting(ShopEntity shop, GetPostingsModel request) {
        List<WbSupplyDetails> supplies = request.getWbSupplies();

        if (Objects.isNull(supplies) || supplies.isEmpty()) {
            return Collections.emptyList();
        }

        String supplyId = getSupplyIdByShopName(supplies, shop);
        if (Objects.isNull(supplyId)) {
            return Collections.emptyList();
        }

        GetOrdersBySupplyIdResponse response = client.getOrdersBySupplyId(encryptService.decrypt(shop.getApiKey()), supplyId);
        log.info("Успешно получены отправления для {} в количестве {}", shop.getName(), response.getOrders().size());
        var orders = response.getOrders()
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

        //так как потом на основании номера заказа генерируется внутренний qr код, и по нему производится поиск этикетки в конечном pdf
        updateOrderIdByStickerId(orders, shop);
        return orders;
    }

    @Override
    public List<byte[]> getPackagesByPostings(ShopEntity shop, List<PostingInfoModel> postings) {
        List<byte[]> packages = new ArrayList<>();
        Base64.Decoder decoder = Base64.getDecoder();

        List<String> orderIds = postings.stream()
                .map(PostingInfoModel::getOriginalOrderNumber)
                .toList();

        log.info("Попытка получить этикетки по заказам WB: {}", orderIds);

        for (int i = 0; i < orderIds.size(); i += MAX_ID_ORDERS_IN_REQUEST) {
            List<String> batch = orderIds.subList(i, Math.min(i + MAX_ID_ORDERS_IN_REQUEST, orderIds.size()));

            GetStickersRequest request = buildGetStickersRequest(batch);

            GetStickersResponse partResponse = client.getStickers(encryptService.decrypt(shop.getApiKey()),
                    WildberriesConstants.StickerTypes.SVG,
                    WildberriesConstants.StickersSize.MIN_WIDTH,
                    WildberriesConstants.StickersSize.MIN_HEIGHT,
                    request);

            partResponse.getStickers().forEach(sticker -> {
                byte[] decodedBytes = decoder.decode(sticker.getFile());
                byte[] pdfBytes = FileUtils.convertSVGtoPDF(decodedBytes);
                packages.add(pdfBytes);
            });
        }
        log.info("Количество этикеток для магазина {} = {}", shop.getName(), orderIds.size());

        return packages;
    }

    @Override
    public void collectPostingsAwaitingPackaging(ShopEntity shop, List<PostingInfoModel> postings) {
        throw new UnsupportedOperationException();
    }

    private void updateOrderIdByStickerId(List<PostingInfoModel> postings, ShopEntity shop) {
        Map<String, String> orderIdStickerIdMapping = new HashMap<>();

        List<String> orderIds = postings.stream()
                .map(PostingInfoModel::getPostingNumber)
                .toList();

        for (int i = 0; i < orderIds.size(); i += MAX_ID_ORDERS_IN_REQUEST) {
            List<String> batch = orderIds.subList(i, Math.min(i + MAX_ID_ORDERS_IN_REQUEST, orderIds.size()));

            GetStickersRequest request = buildGetStickersRequest(batch);

            GetStickersResponse partResponse = client.getStickers(encryptService.decrypt(shop.getApiKey()),
                    WildberriesConstants.StickerTypes.SVG,
                    WildberriesConstants.StickersSize.MIN_WIDTH,
                    WildberriesConstants.StickersSize.MIN_HEIGHT,
                    request);

            partResponse.getStickers().forEach(sticker -> {
                orderIdStickerIdMapping.put(sticker.getOrderId(), sticker.getPartA().concat(sticker.getPartB()));
            });
        }

        changeOrderIdToStickerId(postings, orderIdStickerIdMapping);
        log.info("Успешно изменено номеров заказа на номера этикеток для магазина {} = {}",
                shop.getName(), orderIdStickerIdMapping.values().size());
    }

    private GetStickersRequest buildGetStickersRequest(List<String> orderIds) {
        return GetStickersRequest.builder()
                .orders(orderIds.stream().map(Long::valueOf).toList())
                .build();
    }

    private void changeOrderIdToStickerId(List<PostingInfoModel> postings,
                                          Map<String, String> orderIdStickerIdMapping) {
        postings.forEach(posting -> {
            posting.setOriginalOrderNumber(posting.getPostingNumber());
            String stickerId = orderIdStickerIdMapping.get(posting.getPostingNumber());
            posting.setPostingNumber(stickerId);
        });
    }

    private String getSupplyIdByShopName(List<WbSupplyDetails> supplies, ShopEntity shop) {
        return supplies.stream()
                .filter(it -> it.getShopName().equalsIgnoreCase(shop.getName()))
                .map(WbSupplyDetails::getSupplyId)
                .findFirst()
                .orElse(null);
    }
}
