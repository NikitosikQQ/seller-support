package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.color.ColorDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.color.ColorSaveRequest;
import ru.seller_support.assignment.controller.dto.request.color.ColorUpdateRequest;
import ru.seller_support.assignment.controller.dto.response.ColorDto;
import ru.seller_support.assignment.service.ColorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/colors")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Slf4j
public class ColorController {

    private final ColorService colorService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ColorDto> getAllColors() {
        return colorService.findAll().stream()
                .map(color -> ColorDto.builder()
                        .id(color.getId())
                        .number(color.getNumber())
                        .name(color.getName())
                        .build())
                .toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveColor(@RequestBody @Valid ColorSaveRequest request) {
        colorService.save(request);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateColor(@RequestBody @Valid ColorUpdateRequest request) {
        colorService.update(request);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteColor(@RequestBody @Valid ColorDeleteRequest request) {
        colorService.delete(request);
    }
}
