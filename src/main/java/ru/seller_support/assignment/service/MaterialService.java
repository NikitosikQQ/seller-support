package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.repository.MaterialRepository;
import ru.seller_support.assignment.controller.dto.request.MaterialDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.MaterialSaveRequest;
import ru.seller_support.assignment.controller.dto.request.MaterialUpdateRequest;
import ru.seller_support.assignment.domain.enums.SortingPostingByParam;
import ru.seller_support.assignment.exception.MaterialChangeException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        return repository.findAll();
    }

    @Transactional
    public void save(MaterialSaveRequest request) {
        checkNameInNotUnique(request.getName());

        MaterialEntity material = new MaterialEntity();
        material.setName(request.getName());
        material.setSeparatorName(request.getSeparatorName());
        material.setSortingPostingBy(SortingPostingByParam.of(request.getSortingPostingBy()));

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
        material.setSeparatorName(request.getSeparatorName());
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
}
