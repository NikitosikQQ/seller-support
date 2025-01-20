package ru.seller_support.assignment.adapter.marketplace.ozon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.seller_support.assignment.adapter.marketplace.ozon.inner.Posting;
import ru.seller_support.assignment.adapter.marketplace.ozon.inner.Product;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OzonAdapterMapper {

    String ARTICLE_SEPARATOR = "/";

    @Mapping(target = "marketplace", constant = "OZON")
    @Mapping(target = "palletNumber", source = "palletNumber")
    PostingInfoModel toPostingInfoModel(Posting posting,
                                        Integer palletNumber);

    @Mapping(target = "price", source = "product.price", qualifiedByName = "price")
    @Mapping(target = "totalPrice", expression = "java(getTotalPrice(product))")
    @Mapping(target = "article", source = "product.offerId")
    @Mapping(target = "color", source = "product.offerId", qualifiedByName = "color")
    @Mapping(target = "colorNumber", source = "product.offerId", qualifiedByName = "colorNumber")
    @Mapping(target = "length", source = "product.offerId", qualifiedByName = "length")
    @Mapping(target = "width", source = "product.offerId", qualifiedByName = "width")
    @Mapping(target = "promoName", source = "product.offerId", qualifiedByName = "promoName")
    @Mapping(target = "areaInMeters", ignore = true)
    @Mapping(target = "pricePerSquareMeter", ignore = true)
    ProductModel toProductModel(Product product);

    @Named("color")
    default String getColor(String offerId) {
        return offerId.split(ARTICLE_SEPARATOR)[1];
    }

    @Named("colorNumber")
    default Integer getColorNumber(String offerId) {
        return Integer.parseInt(offerId.split(ARTICLE_SEPARATOR)[2]);
    }

    @Named("length")
    default Integer getLength(String offerId) {
        return Integer.parseInt(offerId.split(ARTICLE_SEPARATOR)[3]);
    }

    @Named("width")
    default Integer getWidth(String offerId) {
        return Integer.parseInt(offerId.split(ARTICLE_SEPARATOR)[4]);
    }

    @Named("promoName")
    default String getPromoName(String offerId) {
        return offerId.split(ARTICLE_SEPARATOR)[6];
    }

    @Named("price")
    default BigDecimal getPrice(String price) {
        return new BigDecimal(price).setScale(2, RoundingMode.HALF_UP);
    }

    default BigDecimal getTotalPrice(Product product) {
        BigDecimal price = new BigDecimal(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal multiplier = BigDecimal.valueOf(product.getQuantity());
        return price.multiply(multiplier);
    }


}
