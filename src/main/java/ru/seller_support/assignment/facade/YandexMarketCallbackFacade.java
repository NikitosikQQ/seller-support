package ru.seller_support.assignment.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;
import ru.seller_support.assignment.service.callback.yandexmarket.BaseYandexMarketCallbackHandler;

import java.util.List;

import static ru.seller_support.assignment.service.callback.yandexmarket.BaseYandexMarketCallbackHandler.baseYandexOkResponse;

@Service
@RequiredArgsConstructor
public class YandexMarketCallbackFacade {

    private final List<BaseYandexMarketCallbackHandler> callbackHandlers;

    public YandexMarketCallbackResponse process(YandexMarketCallbackRequest request) {
        var handler = getCallbackHandlerByType(request.getNotificationType());
        return handler == null ? baseYandexOkResponse() : handler.handle(request);
    }

    private BaseYandexMarketCallbackHandler getCallbackHandlerByType(String callbackType) {
        return callbackHandlers.stream()
                .filter(c -> c.getType().equals(callbackType))
                .findFirst()
                .orElse(null);
    }
}
