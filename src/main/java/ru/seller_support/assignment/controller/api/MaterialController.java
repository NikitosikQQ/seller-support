package ru.seller_support.assignment.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.material.MaterialDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.material.MaterialSaveRequest;
import ru.seller_support.assignment.controller.dto.request.material.MaterialUpdateRequest;
import ru.seller_support.assignment.controller.dto.response.MaterialResponse;
import ru.seller_support.assignment.service.MaterialService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/materials")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Slf4j
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MaterialResponse> getMaterials() {
        return materialService.findAll()
                .stream().map(material -> MaterialResponse.builder()
                        .id(material.getId())
                        .name(material.getName())
                        .separatorName(material.getSeparatorName())
                        .sortingPostingBy(material.getSortingPostingBy().getValue())
                        .useInChpuTemplate(material.getUseInChpuTemplate())
                        .chpuMaterialName(material.getChpuMaterialName())
                        .chpuArticleNumber(material.getChpuArticleNumber())
                        .isOnlyPackaging(material.getIsOnlyPackaging())
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
        log.info("Пришел запрос: {}", request);
        materialService.update(request);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteArticle(@RequestBody @Valid MaterialDeleteRequest request) {
        materialService.delete(request);
    }
}
