package ru.seller_support.assignment.service.callback.ozon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;
import ru.seller_support.assignment.domain.callback.ozon.OzonMessageTypes;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OzonPingCallbackHandler extends BaseOzonCallbackHandler {

    private static final String APPLICATION_NAME = "seller-supp";
    private static final String VERSION = "1.0";

    @Override
    public String getType() {
        return OzonMessageTypes.TYPE_PING;
    }

    @Override
    public OzonCallbackResponse handle(OzonCallbackRequest request) {
        return OzonCallbackResponse.builder()
                .name(APPLICATION_NAME)
                .version(VERSION)
                .time(Instant.now())
                .build();
    }
}
