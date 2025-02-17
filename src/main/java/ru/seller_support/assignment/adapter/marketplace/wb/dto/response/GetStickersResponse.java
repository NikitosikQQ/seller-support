package ru.seller_support.assignment.adapter.marketplace.wb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.inner.Sticker;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetStickersResponse {

    //список этикеток
    @JsonProperty(required = true)
    List<Sticker> stickers;

}
