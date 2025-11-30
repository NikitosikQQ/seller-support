package ru.seller_support.assignment.service.callback.yandexmarket;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;
import ru.seller_support.assignment.domain.callback.yandexmarket.YandexMarketHandleableCallbackStatuses;
import ru.seller_support.assignment.domain.callback.yandexmarket.YandexMarketNotificationsTypes;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.service.order.OrderService;
import ru.seller_support.assignment.service.order.OrderStatusHandler;

@Service
@RequiredArgsConstructor
public class YandexMarketOrderStatusUpdatedCallbackHandler extends BaseYandexMarketCallbackHandler {

    private static final String AUTHOR_OF_CHANGE = "Yandex Market";

    private final OrderService orderService;
    private final OrderStatusHandler orderStatusHandler;

    @Override
    public String getType() {
        return YandexMarketNotificationsTypes.ORDER_STATUS_UPDATED;
    }

    @Override
    public YandexMarketCallbackResponse handle(YandexMarketCallbackRequest request) {
        var originalOrderNumber = request.getOriginalOrderNumber();
        var newRequestStatus = request.getStatus();
        if (originalOrderNumber == null || !StringUtils.hasText(newRequestStatus)) {
            throw new ValidationException("Не указаны обязательные параметры запроса - номер отправления или его статус");
        }

        if (!YandexMarketHandleableCallbackStatuses.HANDLEABLE_STATUSES.contains(newRequestStatus)) {
            return baseYandexOkResponse();
        }

        var externalOrderNumber = String.valueOf(originalOrderNumber);

        var orders = orderService.findAllByExternalOrderNumber(externalOrderNumber);
        if (CollectionUtils.isEmpty(orders) || orders.stream().anyMatch(order -> order.getMarketplace() != Marketplace.YANDEX_MARKET)) {
            return baseYandexOkResponse();
        }

        var newOrderStatus = YandexMarketHandleableCallbackStatuses.YANDEX_MARKET_STATUS_ORDER_STATUS_MAP.get(newRequestStatus);
        orderStatusHandler.updateOrdersStatusAndSave(orders, newOrderStatus, AUTHOR_OF_CHANGE);

        return baseYandexOkResponse();
    }
}
