package ru.seller_support.assignment.adapter.marketplace.ozon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.Posting;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.Product;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.util.CommonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OzonAdapterMapper {

    String ARTICLE_SEPARATOR = "/";

    @Mapping(target = "marketplace", constant = "OZON")
    @Mapping(target = "palletNumber", source = "shop.palletNumber")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "product", expression = "java(getProduct(posting, shop))")
    PostingInfoModel toPostingInfoModel(Posting posting,
                                        ShopEntity shop);

    @Mapping(target = "marketplace", constant = "OZON")
    @Mapping(target = "palletNumber", source = "shop.palletNumber")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "product", expression = "java(getWrongProduct(posting, shop))")
    PostingInfoModel toWrongPostingInfoModel(Posting posting,
                                             ShopEntity shop);

    @Mapping(target = "price", source = "product.price", qualifiedByName = "price")
    @Mapping(target = "totalPrice", expression = "java(getTotalPrice(product))")
    @Mapping(target = "article", source = "product.offerId")
    @Mapping(target = "color", source = "product.offerId", qualifiedByName = "color")
    @Mapping(target = "colorNumber", source = "product.offerId", qualifiedByName = "colorNumber")
    @Mapping(target = "length", source = "product.offerId", qualifiedByName = "length")
    @Mapping(target = "width", source = "product.offerId", qualifiedByName = "width")
    @Mapping(target = "promoName", source = "product.offerId", qualifiedByName = "promoName")
    @Mapping(target = "comment", source = "product.offerId", qualifiedByName = "comment")
    @Mapping(target = "areaInMeters", ignore = true)
    @Mapping(target = "pricePerSquareMeter", ignore = true)
    ProductModel toProductModel(Product product);

    @Named("color")
    default String getColor(String offerId) {
        try {
            return offerId.split(ARTICLE_SEPARATOR)[1];
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из озона %s: %s",
                    offerId, e.getMessage()));
        }
    }

    @Named("colorNumber")
    default Integer getColorNumber(String offerId) {
        try {
            return Integer.parseInt(offerId.split(ARTICLE_SEPARATOR)[2]);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из озона %s: %s",
                    offerId, e.getMessage()));
        }
    }

    @Named("length")
    default Integer getLength(String offerId) {
        try {
            return Integer.parseInt(offerId.split(ARTICLE_SEPARATOR)[3]);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из озона %s : %s",
                    offerId,
                    e.getMessage()));
        }
    }

    @Named("width")
    default Integer getWidth(String offerId) {
        try {
            return Integer.parseInt(offerId.split(ARTICLE_SEPARATOR)[4]);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из озона %s : %s",
                    offerId,
                    e.getMessage()));
        }
    }

    @Named("promoName")
    default String getPromoName(String offerId) {
        try {
            String promoName = offerId.split(ARTICLE_SEPARATOR)[6];
            int firstSpaceIndex = promoName.indexOf(CommonUtils.SPACE);
            if (firstSpaceIndex == -1) {
                return promoName;
            }
            return promoName.substring(0, firstSpaceIndex);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из озона %s : %s",
                    offerId,
                    e.getMessage()));
        }

    }

    @Named("comment")
    default String getComment(String offerId) {
        try {
            String promoName = offerId.split(ARTICLE_SEPARATOR)[6];
            int firstSpaceIndex = promoName.indexOf(CommonUtils.SPACE);
            if (firstSpaceIndex == -1) {
                return CommonUtils.EMPTY_STRING;
            }
            return promoName.substring(firstSpaceIndex + 1);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из озона %s : %s",
                    offerId,
                    e.getMessage()));
        }
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

    default ProductModel getProduct(Posting posting, ShopEntity shop) {
        if (posting.getProducts().size() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Количество артикулов в отправлении %s магазина %s не равна 1",
                    posting.getPostingNumber(), shop.getName()));
        }
        return toProductModel(posting.getProducts().getFirst());
    }

    default ProductModel getWrongProduct(Posting posting, ShopEntity shop) {
        if (posting.getProducts().size() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Количество артикулов в отправлении %s магазина %s не равна 1",
                    posting.getPostingNumber(), shop.getName()));
        }
        return ProductModel.builder()
                .article(posting.getProducts().getFirst().getOfferId())
                .wrongArticle(true)
                .build();
    }

}
