package ru.seller_support.assignment.controller;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.controller.dto.request.GeneratePostingsReportRequest;
import ru.seller_support.assignment.service.processor.MarketplaceReportsProcessor;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private static final String ZIP_NAME = "Postings";

    private final MarketplaceReportsProcessor marketplaceReportsProcessor;

    @PostMapping(value = "/postings", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> test(@RequestBody GeneratePostingsReportRequest request) {
        log.info("Получен запрос на генерацию файлов по отправлениям на отгрузку: {}", request);

        if (Objects.nonNull(request.getExcludeFromOzon()) && Objects.nonNull(request.getExcludeToOzon())
                && request.getExcludeFromOzon().isAfter(request.getExcludeToOzon())) {
            throw new ValidationException("Начало периода исключения заказов озон больше окончания периода");
        }
        byte[] zip = marketplaceReportsProcessor.getNewPostings(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", ZIP_NAME);

        log.info("Успешно сгенерирован архив по отправлениям на отгрузку по запросу {}", request);
        return new ResponseEntity<>(zip, headers, HttpStatus.OK);
    }
}
