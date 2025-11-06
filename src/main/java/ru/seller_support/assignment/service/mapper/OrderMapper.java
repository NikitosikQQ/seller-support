package ru.seller_support.assignment.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderChangesHistoryEntity;
import ru.seller_support.assignment.adapter.postgres.entity.order.OrderEntity;
import ru.seller_support.assignment.controller.dto.request.order.OrderHistoryDto;
import ru.seller_support.assignment.controller.dto.response.order.OrderDto;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    String DEFAULT_SYSTEM_AUTHOR = "SYSTEM";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "number", source = "postingNumber")
    @Mapping(target = "shopName", source = "shopName")
    @Mapping(target = "palletNumber", source = "palletNumber")
    @Mapping(target = "inProcessAt", source = "inProcessAt")
    @Mapping(target = "marketplace", source = "marketplace")
    @Mapping(target = "article", source = "product.article")
    @Mapping(target = "quantity", source = "product.quantity")
    @Mapping(target = "length", source = "product.length")
    @Mapping(target = "width", source = "product.width")
    @Mapping(target = "thickness", source = "product.thickness")
    @Mapping(target = "totalPrice", source = "product.totalPrice")
    @Mapping(target = "areaInMeters", source = "product.areaInMeters")
    @Mapping(target = "pricePerSquareMeter", source = "product.pricePerSquareMeter")
    @Mapping(target = "colorNumber", source = "product.colorNumber")
    @Mapping(target = "color", source = "product.color")
    @Mapping(target = "comment", source = "product.comment")
    @Mapping(target = "materialName", source = "product.materialName")
    @Mapping(target = "promoName", source = "product.promoName")
    OrderEntity toNewEntity(PostingInfoModel model);

    @Mapping(target = "orderStatus", source = "entity.status")
    @Mapping(target = "product", source = "entity")
    @Mapping(target = "postingNumber", expression = "java(getOrderNumber(entity, needOriginalOrderNumber))")
    PostingInfoModel toPostingModel(OrderEntity entity, boolean needOriginalOrderNumber);

    @Mapping(target = "price", expression = "java(getPrice(entity))")
    @Mapping(target = "wrongBox", ignore = true)
    @Mapping(target = "wrongArticle", ignore = true)
    @Mapping(target = "marketplaceProductId", ignore = true)
    ProductModel toProduct(OrderEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "workplace", ignore = true)
    @Mapping(target = "author", constant = DEFAULT_SYSTEM_AUTHOR)
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderNumber", source = "number")
    OrderChangesHistoryEntity toInitialOrderHistory(OrderEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orderId", source = "entity.id")
    @Mapping(target = "orderNumber", source = "entity.number")
    OrderChangesHistoryEntity toOrderHistory(OrderEntity entity, String author, Workplace workplace);

    @Mapping(target = "workplace", source = "entity", qualifiedByName = "getWorkplace")
    OrderHistoryDto toHistoryDto(OrderChangesHistoryEntity entity);

    OrderDto toDto(OrderEntity entity);

    default String getOrderNumber(OrderEntity order, boolean needOriginalOrderNumber) {
        var marketplace = order.getMarketplace();
        var number = order.getNumber();

        if (needOriginalOrderNumber
                && marketplace == Marketplace.YANDEX_MARKET
                && number != null) {
            int dashIndex = number.indexOf('-');
            if (dashIndex != -1) {
                return number.substring(0, dashIndex);
            }
        }

        return number;
    }

    default BigDecimal getPrice(OrderEntity order) {
        var totalPrice = order.getTotalPrice();
        var quantity = BigDecimal.valueOf(order.getQuantity());
        return totalPrice.divide(quantity, 2, RoundingMode.HALF_UP);
    }

    @Named("getWorkplace")
    default String getWorkplace(OrderChangesHistoryEntity history) {
        return Objects.isNull(history.getWorkplace()) ? null : history.getWorkplace().getValue();
    }
}
