package ru.seller_support.assignment.adapter.marketplace.ozon.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectPostingsRequest {

    @JsonProperty(required = true, value = "posting_number")
    private String postingNumber;

    @JsonProperty(required = true, value = "packages")
    private List<Package> packages;


    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Package {

        @JsonProperty(required = true, value = "products")
        private List<Product> products;
    }

    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Product {

        @JsonProperty(required = true, value = "product_id")
        private Long productId;

        @JsonProperty(required = true, value = "quantity")
        private Integer quantity;
    }
}
