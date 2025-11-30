package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.controller.dto.response.order.ResultInformationResponse;
import ru.seller_support.assignment.domain.enums.OrderStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderAlertService {

    private static final String ALERT_MATERIAL_TEMPLATE = "ВНИМАНИЕ! Этот материал - %s";
    private static final String ALERT_ORDER_CANCELLED_STATUS_TEMPLATE = "ВНИМАНИЕ! Заказ %s был отменен, необходимо прекратить обработку заказа";
    private static final String ALERT_ORDER_DONE_STATUS_TEMPLATE = "ВНИМАНИЕ! Заказ %s уже отправлен на маркетплейс, необходимо прекратить обработку заказа";

    public ResultInformationResponse getAlertOrderInFinalStatus(OrderEntity order) {
        return new ResultInformationResponse()
                .setNeedAlert(true)
                .setOrderWasUpdated(false)
                .setMessage(String.format(
                        order.getStatus() == OrderStatus.CANCELLED ? ALERT_ORDER_CANCELLED_STATUS_TEMPLATE : ALERT_ORDER_DONE_STATUS_TEMPLATE,
                        order.getNumber()));
    }

    public ResultInformationResponse getAlertByMaterialOnOrder(OrderEntity order) {
        return new ResultInformationResponse()
                .setNeedAlert(true)
                .setOrderWasUpdated(false)
                .setMessage(String.format(ALERT_MATERIAL_TEMPLATE, order.getMaterialName()));
    }

    public ResultInformationResponse getOrderNotFoundAlert(String orderNumber) {
        return new ResultInformationResponse()
                .setNeedAlert(true)
                .setOrderWasUpdated(false)
                .setMessage(String.format("Не найдено заказа %s в системе seller-supp", orderNumber));
    }

    public ResultInformationResponse getResponseWithoutSoundAlert(boolean wasUpdate, OrderEntity order) {
        return new ResultInformationResponse()
                .setNeedAlert(false)
                .setOrderWasUpdated(wasUpdate)
                .setMessage(wasUpdate
                        ? String.format("Статус заказа %s обновлен на %s", order.getNumber(), order.getStatus())
                        : String.format("Статус заказа %s НЕ БЫЛ обновлен, текущий статус: %s", order.getNumber(), order.getStatus())
                );
    }
}
