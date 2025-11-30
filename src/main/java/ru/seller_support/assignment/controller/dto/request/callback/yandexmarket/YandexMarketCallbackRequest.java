package ru.seller_support.assignment.controller.dto.request.callback.yandexmarket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexMarketCallbackRequest {

    @JsonProperty(value = "notificationType")
    private String notificationType;

    @JsonProperty(value = "orderId")
    private Long originalOrderNumber; // именно оригинальный номер заказа, а не номер грузоместа!

    @JsonProperty(value = "status")
    private String status;

}
