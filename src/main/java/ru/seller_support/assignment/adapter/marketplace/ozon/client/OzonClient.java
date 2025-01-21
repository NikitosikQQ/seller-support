package ru.seller_support.assignment.adapter.marketplace.ozon.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetUnfulfilledListRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.response.GetUnfulfilledListResponse;

@FeignClient(name = "ozonClient", url = "${app.integrations.marketplaces.ozon.rootUrl}")
public interface OzonClient {

    @PostMapping("/v3/posting/fbs/unfulfilled/list")
    GetUnfulfilledListResponse getUnfulfilledOrders(@RequestHeader("Api-Key") String apiKey,
                                                    @RequestHeader("Client-Id") String clientId,
                                                    @RequestBody GetUnfulfilledListRequest request);
}
