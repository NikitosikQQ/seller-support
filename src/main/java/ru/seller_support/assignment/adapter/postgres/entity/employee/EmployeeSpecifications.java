package ru.seller_support.assignment.adapter.postgres.entity.employee;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

public class EmployeeSpecifications {

    public static Specification<EmployeeProcessedCapacityEntity> usernameEqual(String username) {
        if (Objects.isNull(username)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("username"), username);
    }

    public static Specification<EmployeeProcessedCapacityEntity> workplaceIn(Collection<Workplace> workplaces) {
        if (CollectionUtils.isEmpty(workplaces)) {
            return null;
        }
        return (root, query, cb) -> root.get("workplace").in(workplaces);
    }


    public static Specification<EmployeeProcessedCapacityEntity> processedAtBetween(LocalDate processedAtFrom, LocalDate processedAtTo) {
        if (processedAtFrom == null && processedAtTo == null) {
            return null;
        }

        return (root, query, cb) -> {
            Path<LocalDate> path = root.get("processedAt");

            if (processedAtFrom != null && processedAtTo != null) {
                return cb.between(path, processedAtFrom, processedAtTo);
            } else if (processedAtFrom != null) {
                return cb.greaterThanOrEqualTo(path, processedAtFrom);
            } else {
                return cb.lessThanOrEqualTo(path, processedAtTo);
            }
        };
    }
}
