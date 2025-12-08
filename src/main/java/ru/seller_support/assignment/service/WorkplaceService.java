package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceMaterialRatesEntity;
import ru.seller_support.assignment.adapter.postgres.entity.workplace.WorkplaceRatesEntity;
import ru.seller_support.assignment.adapter.postgres.repository.workplace.WorkplaceMaterialRatesRepository;
import ru.seller_support.assignment.adapter.postgres.repository.workplace.WorkplaceRatesRepository;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceChangeRequest;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesChangeBody;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesDeleteBody;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesSaveBody;
import ru.seller_support.assignment.controller.dto.response.MaterialCoefficientResponseDto;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
    public List<MaterialCoefficientResponseDto> findCoefficientPerMaterialsByWorkplace(Workplace workplace) {
        var workplaceMaterialRates = workplaceMaterialRatesRepository.findByWorkplace(workplace);
        return workplaceMaterialRates.stream()
                .map(rate -> new MaterialCoefficientResponseDto()
                        .setId(rate.getId())
                        .setMaterialName(rate.getMaterialName())
                        .setCoefficient(rate.getCoefficient())
                        .setMinArea(rate.getMinAreaInMeters())
                        .setMaxArea(rate.getMaxAreaInMeters()))
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkplaceMaterialRatesEntity findWorkplaceMaterialRatesById(UUID id) {
        return workplaceMaterialRatesRepository.findById(id)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<WorkplaceMaterialRatesEntity> findWorkplaceMaterialRatesByWorkplaceAndMaterial(Workplace workplace,
                                                                                               String materialName) {
        return workplaceMaterialRatesRepository.findAllByWorkplaceAndMaterialName(workplace, materialName);
    }

    @Transactional
    public UUID saveCoefficientRatePerMaterial(WorkplaceMaterialRatesSaveBody request) {
        validateSaveWorkplaceMaterialRatesRequest(null,
                request.getWorkplace(), request.getMaterialName(),
                request.getMinAreaInMeters(), request.getMaxAreaInMeters());

        var entityForSave = new WorkplaceMaterialRatesEntity()
                .setWorkplace(Workplace.fromValue(request.getWorkplace()))
                .setMaterialName(request.getMaterialName())
                .setCoefficient(request.getCoefficient())
                .setMinAreaInMeters(request.getMinAreaInMeters())
                .setMaxAreaInMeters(request.getMaxAreaInMeters());
        return workplaceMaterialRatesRepository.save(entityForSave).getId();
    }

    @Transactional
    public void changeCoefficientRatePerMaterial(List<WorkplaceMaterialRatesChangeBody> changes) {
        changes.forEach(request -> validateSaveWorkplaceMaterialRatesRequest(request.getId(),
                request.getWorkplace(), request.getMaterialName(),
                request.getMinAreaInMeters(), request.getMaxAreaInMeters()));

        changes.forEach(request -> {
            var entity = findWorkplaceMaterialRatesById(request.getId());
            if (entity == null) {
                throw new EntityNotFoundException(String.format("Не найден коэффицент с id = %s", request.getId()));
            }

            entity.setCoefficient(request.getCoefficient());
            entity.setMaterialName(request.getMaterialName());
            entity.setMinAreaInMeters(request.getMinAreaInMeters());
            entity.setMaxAreaInMeters(request.getMaxAreaInMeters());

            workplaceMaterialRatesRepository.save(entity);
        });
    }

    @Transactional
    public void deleteCoefficientRates(WorkplaceMaterialRatesDeleteBody request) {
        workplaceMaterialRatesRepository.deleteById(request.getId());
    }

    @Transactional
    public void changeRate(WorkplaceChangeRequest request) {
        var workplace = findById(request.getId());
        workplace.setRate(request.getRate());
        repository.save(workplace);
    }

    @Transactional(readOnly = true)
    public BigDecimal findActualCoefficient(Workplace workplace, String materialName, BigDecimal areaInMeters) {
        var rates = findWorkplaceMaterialRatesByWorkplaceAndMaterial(workplace, materialName);
        if (CollectionUtils.isEmpty(rates)) {
            return BigDecimal.ONE;
        }

        var defaultRate = rates.stream()
                .filter(e -> e.getMinAreaInMeters().compareTo(BigDecimal.ZERO) == 0
                        && e.getMaxAreaInMeters().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .map(WorkplaceMaterialRatesEntity::getCoefficient)
                .orElse(BigDecimal.ONE);

        var matchedRange = rates.stream()
                .filter(e -> !(e.getMinAreaInMeters().compareTo(BigDecimal.ZERO) == 0
                        && e.getMaxAreaInMeters().compareTo(BigDecimal.ZERO) == 0))
                .filter(e -> e.getMinAreaInMeters().compareTo(areaInMeters) <= 0
                        && e.getMaxAreaInMeters().compareTo(areaInMeters) >= 0)
                .findFirst();

        return matchedRange
                .map(WorkplaceMaterialRatesEntity::getCoefficient)
                .orElse(defaultRate);
    }

    private void validateSaveWorkplaceMaterialRatesRequest(UUID rateId,
                                                           String workplaceName, String materialName,
                                                           BigDecimal requestMinArea, BigDecimal requestMaxArea) {
        var workplace = Workplace.fromValue(workplaceName);

        if (requestMinArea.compareTo(requestMaxArea) > 0) {
            throw new ValidationException("Минимальная квадратура не должна быть больше максимальной квадратуры");
        }

        var workplaceMaterialRates = findWorkplaceMaterialRatesByWorkplaceAndMaterial(workplace, materialName)
                .stream()
                .filter(it -> !it.getId().equals(rateId))
                .toList();

        var isSaveDefaultRateForMaterial = requestMinArea.equals(BigDecimal.ZERO) && requestMaxArea.equals(BigDecimal.ZERO);
        if (CollectionUtils.isEmpty(workplaceMaterialRates)) {
            return;
        }

        workplaceMaterialRates.forEach(entity -> {
            var isRateWithoutRangeOfAreas = entity.getMinAreaInMeters().equals(BigDecimal.ZERO) && entity.getMaxAreaInMeters().equals(BigDecimal.ZERO);
            if (isSaveDefaultRateForMaterial && isRateWithoutRangeOfAreas) {
                throw new ValidationException(String.format(
                        "Коэффицент по умолчанию для материала %s и рабочего места %s уже существует, при необходимости его можно обновить",
                        materialName, workplace));
            }
            var entityMin = entity.getMinAreaInMeters();
            var entityMax = entity.getMaxAreaInMeters();

            boolean isRangesOverlap = entityMin.compareTo(requestMaxArea) <= 0 && requestMinArea.compareTo(entityMax) <= 0;

            if (isRangesOverlap) {
                throw new ValidationException(String.format(
                        "Заданный диапазон [%s; %s] пересекается с существующим диапазоном [%s; %s] для материала %s и рабочего места %s",
                        requestMinArea, requestMaxArea,
                        entityMin, entityMax,
                        materialName, workplace
                ));
            }
        });
    }
}
