package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.wb.WildberriesAdapter;
import ru.seller_support.assignment.controller.dto.request.order.VerifyOrderRequest;
import ru.seller_support.assignment.controller.dto.response.order.ResultInformationResponse;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.service.ShopService;

import java.util.List;

import static ru.seller_support.assignment.domain.enums.OrderStatus.FINAL_STATUSES;

@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private static final String WILDBERRIES_AUTHOR = "Wildberries";
    private static final String ALERT_FACADE_MATERIAL_NAME = "фасад";
    private static final ResultInformationResponse VALIDATION_OK = new ResultInformationResponse()
            .setNeedAlert(false)
            .setOrderWasUpdated(null)
            .setMessage(null);


    private final OrderService orderService;
    private final OrderAlertService orderAlertService;
    private final WildberriesAdapter wildberriesAdapter;
    private final OrderStatusHandler orderStatusHandler;
    private final ShopService shopService;

    public ResultInformationResponse validateOrder(VerifyOrderRequest verifyOrderRequest) {
        var order = orderService.findByNumber(verifyOrderRequest.getOrderNumber());
        if (order == null) {
            return orderAlertService.getOrderNotFoundAlert(verifyOrderRequest.getOrderNumber());
        }
        if (FINAL_STATUSES.contains(order.getStatus())) {
            return orderAlertService.getAlertOrderInFinalStatus(order);
        }

        var materialName = order.getMaterialName();
        var needAlertAboutMaterial = !verifyOrderRequest.getIsEmployeePreparedFacade() && materialName.toLowerCase().contains(ALERT_FACADE_MATERIAL_NAME);
        if (needAlertAboutMaterial) {
            return orderAlertService.getAlertByMaterialOnOrder(order);
        }

        //TODO: костыль в связи с отсутствием на 26.11.2025 вебхуков у ВБ и необходимостью узнавать об отменах заказов как можно раньше
        if (order.getMarketplace() == Marketplace.WILDBERRIES) {
            var shopEntity = shopService.findAllByNames(List.of(order.getShopName())).getFirst();
            var actualOrderStatus = wildberriesAdapter.getOrderStatuses(shopEntity, List.of(order)).get(order.getNumber());
            orderStatusHandler.updateStatusAndSave(order, actualOrderStatus, WILDBERRIES_AUTHOR);

            // если вдруг статус изменился на финальный
            if (FINAL_STATUSES.contains(order.getStatus())) {
                return orderAlertService.getAlertOrderInFinalStatus(order);
            }
        }

        return VALIDATION_OK;
    }

}
