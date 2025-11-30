package ru.seller_support.assignment.service.callback.yandexmarket;

import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;

import java.time.Instant;

public abstract class BaseYandexMarketCallbackHandler {

    private static final String APPLICATION_NAME = "seller-supp";
    private static final String VERSION = "1.0";

    public static YandexMarketCallbackResponse baseYandexOkResponse() {
        return YandexMarketCallbackResponse.builder()
                .name(APPLICATION_NAME)
                .version(VERSION)
                .time(Instant.now())
                .build();
    }

    public abstract String getType();

    public abstract YandexMarketCallbackResponse handle(YandexMarketCallbackRequest request);
}
