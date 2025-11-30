package ru.seller_support.assignment.service.callback.ozon;

import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;

public abstract class BaseOzonCallbackHandler {

    public static final OzonCallbackResponse OZON_BASE_OK_RESPONSE = OzonCallbackResponse.builder()
            .result(true)
            .build();

    public abstract String getType();

    public abstract OzonCallbackResponse handle(OzonCallbackRequest request);
}
