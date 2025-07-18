package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.ArticleDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.ArticleSaveRequest;
import ru.seller_support.assignment.controller.dto.request.ArticleUpdateRequest;
import ru.seller_support.assignment.controller.dto.response.ArticleResponse;
import ru.seller_support.assignment.service.ArticlePromoInfoService;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ArticleController {

    private final ArticlePromoInfoService articlePromoInfoService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ArticleResponse> getArticles() {
        return articlePromoInfoService.findAll()
                .stream()
                .map(article -> ArticleResponse.builder()
                        .name(article.getName())
                        .type(article.getType())
                        .materialName(article.getMaterial().getName())
                        .chpuMaterialName(Objects.nonNull(article.getChpuMaterial())
                                ? article.getChpuMaterial().getName()
                                : null)
                        .quantityPerSku(article.getQuantityPerSku())
                        .build())
                .toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveArticle(@RequestBody @Valid ArticleSaveRequest request) {
        articlePromoInfoService.save(request);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateArticle(@RequestBody @Valid ArticleUpdateRequest request) {
        articlePromoInfoService.update(request);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteArticle(@RequestBody @Valid ArticleDeleteRequest request) {
        articlePromoInfoService.delete(request);
    }
}
