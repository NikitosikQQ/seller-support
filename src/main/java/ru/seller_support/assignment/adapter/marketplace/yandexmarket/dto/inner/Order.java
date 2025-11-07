package ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private String id;
    private String creationDate;
    private List<Item> items;
    private Delivery delivery;
    private String status;


    @JsonIgnore
    private boolean wrongBox;

    @JsonIgnore
    private String originalId;
}
