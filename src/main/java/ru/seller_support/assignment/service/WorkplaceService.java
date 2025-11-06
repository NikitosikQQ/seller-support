package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceMaterialRateId;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceMaterialRatesEntity;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceRatesEntity;
import ru.seller_support.assignment.adapter.postgres.repository.workplace.WorkplaceMaterialRatesRepository;
import ru.seller_support.assignment.adapter.postgres.repository.workplace.WorkplaceRatesRepository;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceChangeRequest;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesChangeBody;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkplaceService {

    private final WorkplaceRatesRepository repository;
    private final WorkplaceMaterialRatesRepository workplaceMaterialRatesRepository;

    @Transactional(readOnly = true)
    public List<WorkplaceRatesEntity> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal findRateByWorkplace(Workplace workplace) {
        return repository.findByWorkplace(workplace).getRate();
    }

    @Transactional(readOnly = true)
    public WorkplaceRatesEntity findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Нет рабочего места с id %s", id)));
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> findCoefficientPerMaterialsByWorkplace(Workplace workplace) {
        var workplaceMaterialRates = workplaceMaterialRatesRepository.findByWorkplace(workplace);
        return workplaceMaterialRates.stream()
                .collect(Collectors.toMap(
                        it -> it.getId().getMaterialName(),
                        WorkplaceMaterialRatesEntity::getCoefficient));
    }

    @Transactional(readOnly = true)
    public WorkplaceMaterialRatesEntity findById(WorkplaceMaterialRateId id) {
        return workplaceMaterialRatesRepository.findById(id)
                .orElse(null);
    }

    @Transactional
    public void saveCoefficientRatePerMaterial(WorkplaceMaterialRatesChangeBody request) {
        var id = new WorkplaceMaterialRateId()
                .setWorkplace(Workplace.fromValue(request.getWorkplace()))
                .setMaterialName(request.getMaterialName());
        var workplaceMaterialRates = findById(id);
        if (workplaceMaterialRates != null) {
            workplaceMaterialRates.setCoefficient(request.getCoefficient());
        } else {
            workplaceMaterialRates = new WorkplaceMaterialRatesEntity()
                    .setId(id)
                    .setCoefficient(request.getCoefficient());
        }
        workplaceMaterialRatesRepository.save(workplaceMaterialRates);
    }

    @Transactional
    public void changeRate(WorkplaceChangeRequest request) {
        var workplace = findById(request.getId());
        workplace.setRate(request.getRate());
        repository.save(workplace);
    }


}
