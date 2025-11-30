package ru.seller_support.assignment.adapter.marketplace.wb.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.inner.Order;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.exception.ArticleMappingException;
import ru.seller_support.assignment.util.CommonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        imports = CommonUtils.class)
public interface WildberriesAdapterMapper {

    String ARTICLE_SEPARATOR = "/";

    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "marketplace", constant = "WILDBERRIES")
    @Mapping(target = "palletNumber", source = "shop.palletNumber")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "product", expression = "java(getProduct(order))")
    @Mapping(target = "inProcessAt", expression = "java(CommonUtils.toMoscowLocalDateTime(order.getCreatedAt()))")
    @Mapping(target = "postingNumber", source = "order.id")
    @Mapping(target = "originalOrderNumber", source = "order.id")
    PostingInfoModel toPostingInfoModel(Order order,
                                        ShopEntity shop);

    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "marketplace", constant = "WILDBERRIES")
    @Mapping(target = "palletNumber", source = "shop.palletNumber")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "product", expression = "java(getWrongProduct(order))")
    @Mapping(target = "inProcessAt", expression = "java(CommonUtils.toMoscowLocalDateTime(order.getCreatedAt()))")
    @Mapping(target = "postingNumber", source = "order.id")
    @Mapping(target = "originalOrderNumber", source = "order.id")
    PostingInfoModel toWrongPostingInfoModel(Order order,
                                             ShopEntity shop);

    @Mapping(target = "price", source = "order.convertedPrice", qualifiedByName = "price")
    @Mapping(target = "totalPrice", source = "order.convertedPrice", qualifiedByName = "price")
    @Mapping(target = "article", source = "order.article")
    @Mapping(target = "color", source = "order.article", qualifiedByName = "color")
    @Mapping(target = "colorNumber", source = "order.article", qualifiedByName = "colorNumber")
    @Mapping(target = "length", source = "order.article", qualifiedByName = "length")
    @Mapping(target = "width", source = "order.article", qualifiedByName = "width")
    @Mapping(target = "thickness", source = "order.article", qualifiedByName = "thickness")
    @Mapping(target = "promoName", source = "order.article", qualifiedByName = "promoName")
    @Mapping(target = "comment", source = "order.article", qualifiedByName = "comment")
    @Mapping(target = "areaInMeters", ignore = true)
    @Mapping(target = "pricePerSquareMeter", ignore = true)
    @Mapping(target = "quantity", constant = "1")
    ProductModel toProductModel(Order order);

    @Named("color")
    default String getColor(String article) {
        try {
            return article.split(ARTICLE_SEPARATOR)[1];
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s: %s",
                    article, e.getMessage()));
        }
    }

    @Named("colorNumber")
    default Integer getColorNumber(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[2].trim());
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s: %s",
                    article, e.getMessage()));
        }
    }

    @Named("length")
    default Integer getLength(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[3].trim());
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("width")
    default Integer getWidth(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[4].trim());
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("thickness")
    default Integer getThickness(String article) {
        try {
            return Integer.parseInt(article.split(ARTICLE_SEPARATOR)[5].trim());
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("promoName")
    default String getPromoName(String article) {
        try {
            String promoName = article.split(ARTICLE_SEPARATOR)[6].trim();
            int firstSpaceIndex = promoName.indexOf(CommonUtils.SPACE);
            if (firstSpaceIndex == -1) {
                return promoName;
            }
            return promoName.substring(0, firstSpaceIndex);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s : %s",
                    article,
                    e.getMessage()));
        }

    }

    @Named("comment")
    default String getComment(String article) {
        try {
            String promoName = article.split(ARTICLE_SEPARATOR)[6].trim();
            int firstSpaceIndex = promoName.indexOf(CommonUtils.SPACE);
            if (firstSpaceIndex == -1) {
                return CommonUtils.EMPTY_STRING;
            }
            return promoName.substring(firstSpaceIndex + 1);
        } catch (Exception e) {
            throw new ArticleMappingException(String.format("Не удалось сконвертировать артикул из WB %s : %s",
                    article,
                    e.getMessage()));
        }
    }

    @Named("price")
    default BigDecimal getPrice(Long price) {
        return BigDecimal.valueOf(price).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    default ProductModel getProduct(Order order) {
        return toProductModel(order);
    }

    default ProductModel getWrongProduct(Order order) {
        return ProductModel.builder()
                .article(order.getArticle())
                .wrongArticle(true)
                .build();
    }
}
