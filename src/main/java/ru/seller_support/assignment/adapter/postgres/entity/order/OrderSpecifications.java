package ru.seller_support.assignment.adapter.postgres.entity.order;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OrderSpecifications {

    public static Specification<OrderEntity> numberEqual(String number) {
        if (Objects.isNull(number)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("number"), number);
    }

    public static Specification<OrderEntity> statusIn(Collection<OrderStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return null;
        }
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<OrderEntity> marketplacesIn(List<Marketplace> marketplaces) {
        if (CollectionUtils.isEmpty(marketplaces)) {
            return null;
        }
        return (root, query, cb) -> root.get("marketplace").in(marketplaces);
    }

    public static Specification<OrderEntity> shopNameEqual(String shopName) {
        if (Objects.isNull(shopName)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("shopName")), shopName.toLowerCase());
    }

    public static Specification<OrderEntity> materialNameEqual(String materialName) {
        if (Objects.isNull(materialName)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("materialName"), materialName);
    }

    public static Specification<OrderEntity> materialNameIn(Collection<String> materialNames) {
        if (CollectionUtils.isEmpty(materialNames)) {
            return null;
        }
        return (root, query, cb) -> root.get("materialName").in(materialNames);
    }

    public static Specification<OrderEntity> materialNameNotIn(Collection<String> materialNames) {
        if (CollectionUtils.isEmpty(materialNames)) {
            return null;
        }
        return (root, query, cb) -> cb.not(root.get("materialName").in(materialNames));
    }

    public static Specification<OrderEntity> colorNumberEqual(Integer colorNumber) {
        if (Objects.isNull(colorNumber)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("colorNumber"), colorNumber);
    }

    public static Specification<OrderEntity> thicknessEqual(Integer thickness) {
        if (Objects.isNull(thickness)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("thickness"), thickness);
    }

    public static Specification<OrderEntity> inProcessAtBetween(LocalDateTime inProcessAtFrom, LocalDateTime inProcessAtTo) {
        if (inProcessAtFrom == null && inProcessAtTo == null) {
            return null;
        }

        return (root, query, cb) -> {
            Path<LocalDateTime> path = root.get("inProcessAt");

            if (inProcessAtFrom != null && inProcessAtTo != null) {
                return cb.between(path, inProcessAtFrom, inProcessAtTo);
            } else if (inProcessAtFrom != null) {
                return cb.greaterThanOrEqualTo(path, inProcessAtFrom);
            } else {
                return cb.lessThanOrEqualTo(path, inProcessAtTo);
            }
        };
    }

    public static Specification<OrderEntity> dimensionsLessThanOrEqual(Integer dimensionOne,
                                                                       Integer dimensionTwo,
                                                                       Integer dimensionsTolerance) {
        if (Objects.isNull(dimensionOne) || Objects.isNull(dimensionTwo)) {
            return null;
        }

        return (root, query, cb) -> cb.or(
                cb.and(
                        cb.lessThanOrEqualTo(root.get("length"), dimensionOne - dimensionsTolerance),
                        cb.lessThanOrEqualTo(root.get("width"), dimensionTwo - dimensionsTolerance)
                ),
                cb.and(
                        cb.lessThanOrEqualTo(root.get("length"), dimensionTwo - dimensionsTolerance),
                        cb.lessThanOrEqualTo(root.get("width"), dimensionOne - dimensionsTolerance)
                )
        );
    }
}
