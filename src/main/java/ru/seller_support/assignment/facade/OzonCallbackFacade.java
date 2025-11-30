package ru.seller_support.assignment.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;
import ru.seller_support.assignment.service.callback.ozon.BaseOzonCallbackHandler;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OzonCallbackFacade {

    private final List<BaseOzonCallbackHandler> callbackHandlers;

    public OzonCallbackResponse process(OzonCallbackRequest request) {
        var handler = getCallbackHandlerByType(request.getMessageType());
        return handler == null ? BaseOzonCallbackHandler.OZON_BASE_OK_RESPONSE : handler.handle(request);
    }

    private BaseOzonCallbackHandler getCallbackHandlerByType(String callbackType) {
        return callbackHandlers.stream()
                .filter(c -> c.getType().equals(callbackType))
                .findFirst()
                .orElse(null);
    }
}
