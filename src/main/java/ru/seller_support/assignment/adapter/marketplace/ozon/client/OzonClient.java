package ru.seller_support.assignment.adapter.marketplace.ozon.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetPackagesRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.request.GetUnfulfilledListRequest;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.response.GetUnfulfilledListResponse;

@FeignClient(name = "ozonClient", url = "${app.integrations.marketplaces.ozon.rootUrl}")
public interface OzonClient {

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PostMapping("/v3/posting/fbs/unfulfilled/list")
    GetUnfulfilledListResponse getUnfulfilledOrders(@RequestHeader("Api-Key") String apiKey,
                                                    @RequestHeader("Client-Id") String clientId,
                                                    @RequestBody GetUnfulfilledListRequest request);

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PostMapping(value = "/v2/posting/fbs/package-label", produces = MediaType.APPLICATION_PDF_VALUE)
    byte[] getPackages(@RequestHeader("Api-Key") String apiKey,
                       @RequestHeader("Client-Id") String clientId,
                       @RequestBody GetPackagesRequest request);
}
