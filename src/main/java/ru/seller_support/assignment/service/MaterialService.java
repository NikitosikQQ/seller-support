package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.repository.MaterialRepository;
import ru.seller_support.assignment.controller.dto.request.material.MaterialDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.material.MaterialSaveRequest;
import ru.seller_support.assignment.controller.dto.request.material.MaterialUpdateRequest;
import ru.seller_support.assignment.domain.enums.SortingPostingByParam;
import ru.seller_support.assignment.exception.MaterialChangeException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository repository;

    @Transactional(readOnly = true)
    public MaterialEntity findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "Материал с наименованием '%s' не найден", name)));
    }

    public List<MaterialEntity> findAll() {
        return repository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<MaterialEntity> findAllForChpu() {
        return repository.findAllByUseInChpuTemplate(true);
    }

    public List<MaterialEntity> findAllByIds(Collection<UUID> ids) {
        return repository.findAllByIdIn(ids);
    }

    @Transactional
    public void save(MaterialSaveRequest request) {
        checkNameInNotUnique(request.getName());

        if (request.getIsOnlyPackaging() && request.getUseInChpuTemplate()) {
            throw new ValidationException("Нельзя материалу быть только на упаковке и использоваться в ЧПУ одновременно");
        }

        MaterialEntity material = new MaterialEntity();
        material.setName(request.getName());
        material.setSeparatorName(request.getSeparatorName());
        material.setSortingPostingBy(SortingPostingByParam.of(request.getSortingPostingBy()));
        material.setUseInChpuTemplate(request.getUseInChpuTemplate());
        material.setChpuMaterialName(request.getChpuMaterialName());
        material.setChpuArticleNumber(request.getChpuArticleNumber());
        material.setIsOnlyPackaging(!Objects.isNull(request.getIsOnlyPackaging()) && request.getIsOnlyPackaging());


        repository.save(material);
    }

    @Transactional
    public void update(MaterialUpdateRequest request) {
        checkNameInNotUnique(request.getUpdatedName());
        MaterialEntity material = findByName(request.getCurrentName());
        if (Objects.nonNull(request.getUpdatedName())) {
            material.setName(request.getUpdatedName());
        }
        if (Objects.nonNull(request.getSortingPostingBy())) {
            material.setSortingPostingBy(SortingPostingByParam.of(request.getSortingPostingBy()));
        }
        if (Objects.nonNull(request.getUseInChpuTemplate())) {
            material.setUseInChpuTemplate(request.getUseInChpuTemplate());
        }
        if (Objects.nonNull(request.getChpuMaterialName())) {
            material.setChpuMaterialName(request.getChpuMaterialName());
        }
        if (Objects.nonNull(request.getChpuArticleNumber())) {
            material.setChpuArticleNumber(request.getChpuArticleNumber());
        }
        if (Objects.nonNull(request.getIsOnlyPackaging())) {
            material.setIsOnlyPackaging(request.getIsOnlyPackaging());
        }
        material.setSeparatorName(request.getSeparatorName());

        if (material.getIsOnlyPackaging() && material.getUseInChpuTemplate()) {
            throw new ValidationException("Нельзя материалу быть только на упаковке и использоваться в ЧПУ одновременно");
        }
        repository.save(material);
    }

    @Transactional
    public void delete(MaterialDeleteRequest request) {
        MaterialEntity material = findByName(request.getName());
        repository.delete(material);
    }

    public List<String> getSortingParams() {
        return Arrays.stream(SortingPostingByParam.values())
                .map(SortingPostingByParam::getValue)
                .toList();
    }

    private void checkNameInNotUnique(String name) {
        if (Objects.isNull(name)) {
            return;
        }
        if (repository.findByName(name).isPresent()) {
            throw new MaterialChangeException(String.format(
                    "Наименование материала %s уже существует", name));
        }
    }

    public List<MaterialEntity> findAllOnlyPackaging(Boolean onlyPackaging) {
        return repository.findAllByIsOnlyPackaging(onlyPackaging);
    }
}
