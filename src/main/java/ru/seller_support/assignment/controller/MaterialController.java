package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.MaterialDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.MaterialSaveRequest;
import ru.seller_support.assignment.controller.dto.request.MaterialUpdateRequest;
import ru.seller_support.assignment.controller.dto.response.MaterialResponse;
import ru.seller_support.assignment.service.MaterialService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/materials")
@PreAuthorize("hasRole('ADMIN')")
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MaterialResponse> getMaterials() {
        return materialService.findAll()
                .stream().map(material -> MaterialResponse.builder()
                        .name(material.getName())
                        .separatorName(material.getSeparatorName())
                        .sortingPostingBy(material.getSortingPostingBy().getValue())
                        .build())
                .toList();
    }

    @GetMapping(value = "/sorting-params", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getSortingParams() {
        return materialService.getSortingParams();
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveArticle(@RequestBody @Valid MaterialSaveRequest request) {
        materialService.save(request);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateArticle(@RequestBody @Valid MaterialUpdateRequest request) {
        materialService.update(request);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteArticle(@RequestBody @Valid MaterialDeleteRequest request) {
        materialService.delete(request);
    }
}
