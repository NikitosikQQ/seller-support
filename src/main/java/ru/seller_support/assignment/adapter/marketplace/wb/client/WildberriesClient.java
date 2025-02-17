package ru.seller_support.assignment.adapter.marketplace.wb.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.request.GetStickersRequest;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetOrdersBySupplyIdResponse;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.response.GetStickersResponse;

@FeignClient(name = "wbClient", url = "${app.integrations.marketplaces.wb.rootUrl}")
public interface WildberriesClient {

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @GetMapping("/api/v3/supplies/{supplyId}/orders")
    GetOrdersBySupplyIdResponse getOrdersBySupplyId(@RequestHeader("Authorization") String apiKey,
                                                    @PathVariable("supplyId") String supplyId);

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PostMapping("/api/v3/orders/stickers")
    GetStickersResponse getStickers(@RequestHeader("Authorization") String apiKey,
                                    @RequestParam("type") String type,
                                    @RequestParam("width") Integer width,
                                    @RequestParam("height") Integer height,
                                    @RequestBody GetStickersRequest request);


}
