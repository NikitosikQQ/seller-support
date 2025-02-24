package ru.seller_support.assignment.adapter.marketplace.yandexmarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.common.YandexMarketConstants;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Item;
import ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner.Order;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.util.CommonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface YandexMarketAdapterMapper {

    String ARTICLE_SEPARATOR = "/";

    @Mapping(target = "marketplace", constant = "YANDEX_MARKET")
    @Mapping(target = "palletNumber", source = "shop.palletNumber")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "product", expression = "java(getProduct(order, shop))")
    @Mapping(target = "inProcessAt", source = "order.creationDate", qualifiedByName = "inProcessAt")
    @Mapping(target = "postingNumber", source = "order.id")
    PostingInfoModel toPostingInfoModel(Order order,
                                        ShopEntity shop);

    @Mapping(target = "marketplace", constant = "YANDEX_MARKET")
    @Mapping(target = "palletNumber", source = "shop.palletNumber")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "product", expression = "java(getWrongProduct(order, shop))")
    @Mapping(target = "inProcessAt", source = "order.creationDate", qualifiedByName = "inProcessAt")
    @Mapping(target = "postingNumber", source = "order.id")
    PostingInfoModel toWrongPostingInfoModel(Order order,
                                             ShopEntity shop);

    @Mapping(target = "quantity", source = "item.count")
    @Mapping(target = "price", source = "item.price", qualifiedByName = "price")
    @Mapping(target = "totalPrice", expression = "java(getTotalPrice(item))")
    @Mapping(target = "article", source = "item.offerId")
    @Mapping(target = "color", source = "item.offerId", qualifiedByName = "color")
    @Mapping(target = "colorNumber", source = "item.offerId", qualifiedByName = "colorNumber")
    @Mapping(target = "length", source = "item.offerId", qualifiedByName = "length")
    @Mapping(target = "width", source = "item.offerId", qualifiedByName = "width")
    @Mapping(target = "promoName", source = "item.offerId", qualifiedByName = "promoName")
    @Mapping(target = "comment", source = "item.offerId", qualifiedByName = "comment")
    @Mapping(target = "areaInMeters", ignore = true)
    @Mapping(target = "pricePerSquareMeter", ignore = true)
    ProductModel toProductModel(Item item);

    @Named("color")
    default String getColor(String article) {
        try {
            return article.split(ARTICLE_SEPARATOR)[1];
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из Yandex %s: %s",
                    article, e.getMessage()));
        }
    }

    @Named("colorNumber")
    default Integer getColorNumber(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[2]);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из Yandex %s: %s",
                    article, e.getMessage()));
        }
    }

    @Named("length")
    default Integer getLength(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[3]);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из Yandex %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("inProcessAt")
    default Instant getInProcessAt(String creationDate) {
        LocalDateTime dateTime = LocalDateTime.parse(
                creationDate, YandexMarketConstants.CREATION_DATE_DATE_TIME_FORMATTER);
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    @Named("width")
    default Integer getWidth(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[4]);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из Yandex %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("promoName")
    default String getPromoName(String article) {
        try {
            String promoName = article.split(ARTICLE_SEPARATOR)[6];
            int firstSpaceIndex = promoName.indexOf(CommonUtils.SPACE);
            if (firstSpaceIndex == -1) {
                return promoName;
            }
            return promoName.substring(0, firstSpaceIndex);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из Yandex %s : %s",
                    article,
                    e.getMessage()));
        }

    }

    @Named("comment")
    default String getComment(String article) {
        try {
            String promoName = article.split(ARTICLE_SEPARATOR)[6];
            int firstSpaceIndex = promoName.indexOf(CommonUtils.SPACE);
            if (firstSpaceIndex == -1) {
                return CommonUtils.EMPTY_STRING;
            }
            return promoName.substring(firstSpaceIndex + 1);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из Yandex %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("price")
    default BigDecimal getPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    default BigDecimal getTotalPrice(Item item) {
        BigDecimal price = item.getPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal multiplier = BigDecimal.valueOf(item.getCount());
        return price.multiply(multiplier);
    }

    default ProductModel getProduct(Order order, ShopEntity shop) {
        if (order.getItems().size() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Количество артикулов в отправлении %s магазина %s не равна 1",
                    order.getId(), shop.getName()));
        }
        return toProductModel(order.getItems().getFirst());
    }

    default ProductModel getWrongProduct(Order order, ShopEntity shop) {
        if (order.getItems().size() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Количество артикулов в отправлении %s магазина %s не равна 1",
                    order.getId(), shop.getName()));
        }
        return ProductModel.builder()
                .article(order.getItems().getFirst().getOfferId())
                .wrongArticle(true)
                .build();
    }
}
