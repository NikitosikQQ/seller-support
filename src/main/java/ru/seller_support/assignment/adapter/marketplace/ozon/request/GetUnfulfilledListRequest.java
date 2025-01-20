package ru.seller_support.assignment.adapter.marketplace.ozon.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.adapter.marketplace.ozon.inner.FilterBody;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetUnfulfilledListRequest {

    //направление сортировки - ASC ИЛИ DESC
    @JsonProperty(required = true, value = "dir")
    String dir;

    //фильтр по отправлениям
    @JsonProperty(value = "filter")
    FilterBody filter;

    //лимит на кол-во отправлений в ответе, от 1 до 1000
    @JsonProperty(required = true, value = "limit")
    Integer limit;
}
