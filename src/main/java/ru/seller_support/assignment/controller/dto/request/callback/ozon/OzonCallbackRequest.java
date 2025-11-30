package ru.seller_support.assignment.controller.dto.request.callback.ozon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OzonCallbackRequest {

    @JsonProperty(value = "message_type")
    private String messageType;

    @JsonProperty(value = "posting_number")
    private String postingNumber;

    @JsonProperty(value = "new_state")
    private String newState;

}
