package ru.seller_support.assignment.service.callback.yandexmarket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;
import ru.seller_support.assignment.domain.callback.yandexmarket.YandexMarketNotificationsTypes;

@Service
@RequiredArgsConstructor
public class YandexMarketPingCallbackHandler extends BaseYandexMarketCallbackHandler {

    @Override
    public String getType() {
        return YandexMarketNotificationsTypes.PING;
    }

    @Override
    public YandexMarketCallbackResponse handle(YandexMarketCallbackRequest request) {
        return baseYandexOkResponse();
    }
}
