package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderAlertService {

    private static final String FACADE_MATERIAL_NAME = "фасад";
    private static final String ALERT_MATERIAL_TEMPLATE = "ВНИМАНИЕ! Этот материал - %s";

    public String getAlertByOrder(OrderEntity order) {
        var materialName = order.getMaterialName();
        if (materialName.toLowerCase().contains(FACADE_MATERIAL_NAME)) {
            return String.format(ALERT_MATERIAL_TEMPLATE, materialName);
        }
        return null;
    }
}
