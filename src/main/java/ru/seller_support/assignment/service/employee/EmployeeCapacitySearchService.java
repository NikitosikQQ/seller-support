package ru.seller_support.assignment.service.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeProcessedCapacityEntity;
import ru.seller_support.assignment.adapter.postgres.repository.employee.EmployeeProcessedCapacityRepository;
import ru.seller_support.assignment.controller.dto.request.report.EmployeeCapacitySearchRequest;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.Specification.where;
import static ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeSpecifications.*;

@Service
@RequiredArgsConstructor
public class EmployeeCapacitySearchService {

    private static final Sort SORT_BY_PROCESSED_AT_USERNAME_WORKPLACE = Sort.by(Sort.Direction.ASC, "processedAt")
            .and(Sort.by(Sort.Direction.ASC, "username"))
            .and(Sort.by(Sort.Direction.ASC, "workplace"));

    private final EmployeeProcessedCapacityRepository repository;

    public List<EmployeeProcessedCapacityEntity> search(EmployeeCapacitySearchRequest query) {
        Specification<EmployeeProcessedCapacityEntity> spec = generateSpecFromSearchQuery(query);

        return repository.findAll(spec, SORT_BY_PROCESSED_AT_USERNAME_WORKPLACE);
    }

    private Specification<EmployeeProcessedCapacityEntity> generateSpecFromSearchQuery(EmployeeCapacitySearchRequest query) {
        var workplaces = new ArrayList<Workplace>();
        if (query.getWorkplaces() != null) {
            query.getWorkplaces().forEach(value -> workplaces.add(Workplace.fromValue(value)));
        }
        return where(processedAtBetween(query.getFrom(), query.getTo()))
                .and(usernameEqual(query.getUsername()))
                .and(workplaceIn(workplaces));
    }
}
