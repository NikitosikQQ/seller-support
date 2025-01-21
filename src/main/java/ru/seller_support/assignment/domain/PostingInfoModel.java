package ru.seller_support.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.time.Instant;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PostingInfoModel {
    private Marketplace marketplace;
    private String shopName;
    private Integer palletNumber;
    private String postingNumber;
    private Instant inProcessAt;
    private List<ProductModel> products;
}
