package ru.seller_support.assignment.controller.dto.request.callback.ozon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class OzonCallbackResponse {

    @JsonProperty(value = "result")
    private Boolean result;

    // три нижних поля только для ответа на TYPE_PING

    @JsonProperty(value = "version")
    private String version;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "time")
    private Instant time;

    //в случае ошибки отправляется только этот объект в ответе
    @JsonProperty(value = "error")
    private Error error;

    @Data
    @Accessors(chain = true)
    public static class Error {
        private String code;
        private String message;
        private String details;
    }

}
