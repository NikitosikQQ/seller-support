package ru.seller_support.assignment.service.callback.ozon;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;
import ru.seller_support.assignment.domain.callback.ozon.OzonHandleableCallbackStatuses;
import ru.seller_support.assignment.domain.callback.ozon.OzonMessageTypes;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.service.order.OrderService;
import ru.seller_support.assignment.service.order.OrderStatusHandler;

@Service
@RequiredArgsConstructor
public class OzonStateChangedCallbackHandler extends BaseOzonCallbackHandler {

    private static final String AUTHOR_OF_CHANGE = "Ozon";

    private final OrderService orderService;
    private final OrderStatusHandler orderStatusHandler;

    @Override
    public String getType() {
        return OzonMessageTypes.TYPE_STATE_CHANGED;
    }

    @Override
    public OzonCallbackResponse handle(OzonCallbackRequest request) {
        var orderNumber = request.getPostingNumber();
        var newRequestStatus = request.getNewState();
        if (!StringUtils.hasText(orderNumber) || !StringUtils.hasText(newRequestStatus)) {
            throw new ValidationException("Не указаны обязательные параметры запроса - номер отправления или его статус");
        }

        if (!OzonHandleableCallbackStatuses.HANDLEABLE_STATUSES.contains(newRequestStatus)) {
            return OZON_BASE_OK_RESPONSE;
        }

        var order = orderService.findByNumber(orderNumber);
        if (order == null || order.getMarketplace() != Marketplace.OZON) {
            return OZON_BASE_OK_RESPONSE;
        }

        var newOrderStatus = OzonHandleableCallbackStatuses.OZON_STATUS_ORDER_STATUS_MAP.get(newRequestStatus);
        orderStatusHandler.updateStatusAndSave(order, newOrderStatus, AUTHOR_OF_CHANGE);

        return OZON_BASE_OK_RESPONSE;
    }
}
