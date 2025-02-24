package ru.seller_support.assignment.adapter.marketplace.yandexmarket.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.request.GetShipmentsRequest;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.request.PrepareStickersRequest;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.GetOrdersByIdsResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.GetShipmentsResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.GetStickersByReportIdResponse;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.response.PrepareStickersResponse;

import java.util.List;

@FeignClient(name = "yandexClient", url = "${app.integrations.marketplaces.yandex.rootUrl}")
public interface YandexMarketClient {

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PutMapping(value = "/campaigns/{campaignId}/first-mile/shipments",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    GetShipmentsResponse getShipments(@RequestHeader("Api-Key") String apiKey,
                                      @PathVariable("campaignId") String campaignId,
                                      @RequestBody GetShipmentsRequest request);

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @GetMapping("/campaigns/{campaignId}/orders")
    GetOrdersByIdsResponse getOrdersByIds(@RequestHeader("Api-Key") String apiKey,
                                          @PathVariable("campaignId") String campaignId,
                                          @RequestParam("orderIds") List<Long> orderIds);

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PostMapping("/reports/documents/labels/generate")
    PrepareStickersResponse prepareStickers(@RequestHeader("Api-Key") String apiKey,
                                            @RequestBody PrepareStickersRequest request);

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @GetMapping("/reports/info/{reportId}")
    GetStickersByReportIdResponse getStickers(@RequestHeader("Api-Key") String apiKey,
                                              @PathVariable("reportId") String reportId);


}
