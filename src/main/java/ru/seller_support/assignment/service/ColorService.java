package ru.seller_support.assignment.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.ColorEntity;
import ru.seller_support.assignment.adapter.postgres.repository.ColorRepository;
import ru.seller_support.assignment.controller.dto.request.color.ColorDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.color.ColorSaveRequest;
import ru.seller_support.assignment.controller.dto.request.color.ColorUpdateRequest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColorService {

    private final ColorRepository colorRepository;

    @Transactional(readOnly = true)
    public List<ColorEntity> findAll() {
        return colorRepository.findAllByOrderByNumberAsc();
    }

    @Transactional
    public void save(ColorSaveRequest request) {
        checkNumberIsNotUnique(request.getNumber());
        var color = new ColorEntity()
                .setName(request.getName())
                .setNumber(request.getNumber());
        colorRepository.save(color);
    }

    @Transactional
    public void update(ColorUpdateRequest request) {
        var color = findById(request.getId());
        if (Objects.nonNull(request.getNumber()) && !request.getNumber().equals(color.getNumber())) {
            checkNumberIsNotUnique(request.getNumber());
            color.setNumber(request.getNumber());
        }
        if (Objects.nonNull(request.getName())) {
            color.setName(request.getName());
        }
        colorRepository.save(color);
    }

    @Transactional
    public void delete(ColorDeleteRequest request) {
        colorRepository.deleteById(request.getId());
    }

    @Transactional(readOnly = true)
    public ColorEntity findById(UUID id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Нет цвета с id %s", id)));
    }

    private void checkNumberIsNotUnique(Integer number) {
        if (colorRepository.findByNumber(number).isPresent()) {
            throw new ValidationException("Номер цвета = " + number + " уже используется");
        }
    }
}
