package ru.seller_support.assignment.adapter.marketplace.yandexmarket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.MarketplaceAdapter;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.client.YandexMarketClient;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.common.YandexMarketConstants;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Item;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Order;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Shipment;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.request.GetShipmentsRequest;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.request.PrepareStickersRequest;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.GetOrdersByIdsResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.GetShipmentsResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.GetStickersByReportIdResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.PrepareStickersResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.mapper.YandexMarketAdapterMapper;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.GetPostingsModel;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.util.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexMarketAdapter extends MarketplaceAdapter {

    private static final int MAX_ID_ORDERS_IN_REQUEST = 1000;

    private final YandexMarketClient client;
    private final YandexStickerDownloader stickerDownloader;
    private final YandexMarketAdapterMapper mapper;

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.YANDEX_MARKET;
    }

    @Override
    public List<PostingInfoModel> getNewPosting(ShopEntity shop, GetPostingsModel request) {
        GetShipmentsRequest getShipmentsRequest = buildGetShipmentsRequest(request);
        GetShipmentsResponse shipmentsResponse = client.getShipments(
                shop.getApiKey(), shop.getClientId(), getShipmentsRequest);
        if (Objects.isNull(shipmentsResponse) || shipmentsResponse.getResult().getShipments().isEmpty()) {
            return Collections.emptyList();
        }
        List<Shipment> shipments = shipmentsResponse.getResult().getShipments();
        if (shipments.size() > 1) {
            throw new IllegalArgumentException(String.format("Найдено более 1 отгрузки для Yandex market магазина %s на дату %s",
                    shop.getName(), request.getTo()));
        }
        List<Long> orderIds = shipments.getFirst().getOrderIds();
        GetOrdersByIdsResponse response = client.getOrdersByIds(shop.getApiKey(), shop.getClientId(), orderIds);
        List<Order> originalOrders = response.getOrders();
        List<Order> splittedOrders = splitOrders(originalOrders);

        log.info("Успешно получены отправления для {} в количестве {}", shop.getName(), originalOrders.size());

        return splittedOrders.stream()
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
        List<String> orderIds = postings.stream()
                .map(PostingInfoModel::getPostingNumber)
                .toList();

        for (int i = 0; i < orderIds.size(); i += MAX_ID_ORDERS_IN_REQUEST) {
            List<String> batch = orderIds.subList(i, Math.min(i + MAX_ID_ORDERS_IN_REQUEST, orderIds.size()));

            PrepareStickersRequest request = buildPrepareStickersRequest(batch, shop);

            PrepareStickersResponse prepareResponse = client.prepareStickers(shop.getApiKey(), request);

            byte[] pdfBytes = downloadStickers(prepareResponse, shop);
            packages.add(pdfBytes);
        }
        log.info("Количество этикеток для магазина {} = {}", shop.getName(), orderIds.size());

        return packages;
    }

    private byte[] downloadStickers(PrepareStickersResponse info, ShopEntity shop) {
        try {
            Thread.sleep(info.getResult().getEstimatedGenerationTime());
            GetStickersByReportIdResponse stickers = client.getStickers(shop.getApiKey(), info.getResult().getReportId());
            return stickerDownloader.downloadStickersFromUrl(stickers.getResult().getFile());
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                    "Ошибка при попытке скачивания этикеток YANDEX по магазину %s: %s",
                    shop.getName(), e.getMessage()), e);
        }
    }

    private List<Order> splitOrders(List<Order> orders) {
        List<Order> result = new ArrayList<>();

        for (Order order : orders) {
            if (order.getItems().size() <= 1) {
                result.add(order);
            } else {
                for (Item item : order.getItems()) {
                    Order newOrder = order.toBuilder()
                            .items(Collections.singletonList(item))
                            .build();
                    result.add(newOrder);
                }
            }
        }

        return result;
    }

    private GetShipmentsRequest buildGetShipmentsRequest(GetPostingsModel request) {
        return GetShipmentsRequest.builder()
                .dateFrom(CommonUtils.formatInstantToString(
                        YandexMarketConstants.GET_SHIPMENT_DATE_FORMATTER, request.getTo()))
                .dateTo(CommonUtils.formatInstantToString(
                        YandexMarketConstants.GET_SHIPMENT_DATE_FORMATTER, request.getTo()))
                .status(YandexMarketConstants.ShipmentStatus.OUTBOUND_READY_FOR_CONFIRMATION)
                .cancelledOrders(false)
                .build();
    }

    private PrepareStickersRequest buildPrepareStickersRequest(List<String> orderIds, ShopEntity shop) {
        List<Long> ids = orderIds.stream()
                .map(Long::valueOf)
                .toList();

        return PrepareStickersRequest.builder()
                .orderIds(ids)
                .businessId(shop.getBusinessId())
                .build();
    }
}
