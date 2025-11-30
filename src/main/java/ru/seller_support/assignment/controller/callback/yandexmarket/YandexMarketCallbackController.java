package ru.seller_support.assignment.controller.callback.yandexmarket;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;
import ru.seller_support.assignment.facade.YandexMarketCallbackFacade;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/yandex-market")
public class YandexMarketCallbackController {

    private final YandexMarketCallbackFacade facade;

    @PostMapping(path = "/callback")
    public ResponseEntity<YandexMarketCallbackResponse> processCallback(@RequestBody YandexMarketCallbackRequest request) {
        var result = facade.process(request);
        return ResponseEntity.ok(result);
    }
}
