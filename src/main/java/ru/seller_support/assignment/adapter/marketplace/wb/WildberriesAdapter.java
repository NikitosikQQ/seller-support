package ru.seller_support.assignment.adapter.marketplace.wb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.wb.client.WildberriesClient;
import ru.seller_support.assignment.adapter.marketplace.wb.common.WildberriesConstants;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.request.GetOrderStatusRequest;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.request.GetStickersRequest;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.request.SearchOrdersRequest;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetStickersResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.SupplyInfoResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.mapper.WildberriesAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.controller.dto.request.WbSupplyDetails;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.service.TextEncryptService;
import ru.seller_support.assignment.util.FileUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WildberriesAdapter extends MarketplaceAdapter {

    private static final int MAX_ID_ORDERS_IN_REQUEST = 100;
    private static final long MAX_ORDER_LIMIT_IN_RESPONSE = 1000;
    private static final long DEFAULT_NEXT_VALUE = 0;

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

        SupplyInfoResponse supplyResponse = client.getSupplyById(encryptService.decrypt(shop.getApiKey()), supplyId);

        Instant supplyCreatedDate = supplyResponse.getCreatedAt();
        var searchOrdersRequest = buildSearchOrdersRequest(supplyCreatedDate);
        var searchOrderResponse = client.getOrders(encryptService.decrypt(shop.getApiKey()),
                searchOrdersRequest.getLimit(),
                searchOrdersRequest.getNext(),
                searchOrdersRequest.getDateFrom(),
                searchOrdersRequest.getDateTo());
        var filteredBySupplyIdOrders = searchOrderResponse.getOrders().stream()
                .filter(it -> it.getSupplyId().equalsIgnoreCase(supplyId))
                .toList();

        log.info("Успешно получены отправления для {} в количестве {}", shop.getName(), filteredBySupplyIdOrders.size());
        var orders = filteredBySupplyIdOrders.stream()
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

    public Map<String, OrderStatus> getOrderStatuses(ShopEntity shop, List<OrderEntity> orders) {
        log.info("Получаем статусы заказов из WB...");
        var externalOrderNumberMap = orders.stream()
                .collect(Collectors.toMap(order -> Long.parseLong(order.getExternalOrderNumber()), Function.identity()));

        var request = new GetOrderStatusRequest().setOrders(externalOrderNumberMap.keySet().stream().toList());
        var response = client.getOrdersStatus(encryptService.decrypt(shop.getApiKey()), request);
        var responseOrdersInfo = response.getOrders();

        if (CollectionUtils.isEmpty(responseOrdersInfo)) {
            log.warn("При получении статусов WB заказов они не были найдены");
            return orders.stream()
                    .collect(Collectors.toMap(OrderEntity::getNumber, OrderEntity::getStatus));
        }

        var resultMap = new HashMap<String, OrderStatus>();
        for (var wbOrderInfo : responseOrdersInfo) {
            var actualWbStatus = wbOrderInfo.getWbStatus();
            var orderEntity = externalOrderNumberMap.get(wbOrderInfo.getId());
            var actualOrderStatus = WildberriesOrderHandleableStatuses.WB_STATUS_ORDER_STATUS_MAP.getOrDefault(actualWbStatus,
                    orderEntity.getStatus());

            resultMap.put(orderEntity.getNumber(), actualOrderStatus);
        }

        log.info("Получены следующие статусы WB заказов: {}", resultMap);
        return resultMap;
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

    private SearchOrdersRequest buildSearchOrdersRequest(Instant supplyCreatedDate) {
        Instant minOrderCreatedDate = supplyCreatedDate.minus(3, ChronoUnit.DAYS);

        Long dateTo = supplyCreatedDate.getEpochSecond();
        Long dateFrom = minOrderCreatedDate.getEpochSecond();
        return SearchOrdersRequest.builder()
                .limit(MAX_ORDER_LIMIT_IN_RESPONSE)
                .next(DEFAULT_NEXT_VALUE)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();
    }

}
