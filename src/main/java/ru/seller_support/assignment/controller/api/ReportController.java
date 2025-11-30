package ru.seller_support.assignment.controller.api;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.controller.dto.request.order.SearchOrderRequest;
import ru.seller_support.assignment.controller.dto.request.report.EmployeeCapacitySearchRequest;
import ru.seller_support.assignment.controller.dto.request.report.GenerateChpuTemplateRequest;
import ru.seller_support.assignment.controller.dto.request.report.GeneratePostingsReportRequest;
import ru.seller_support.assignment.service.processor.MarketplaceReportsProcessor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private static final String ZIP_NAME = "Postings";
    private static final String EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final MarketplaceReportsProcessor marketplaceReportsProcessor;

    @PostMapping(value = "/postings", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> test(@RequestBody GeneratePostingsReportRequest request) {
        log.info("Получен запрос на генерацию файлов по отправлениям на отгрузку: {}", request);

        if (Objects.nonNull(request.getExcludeFromOzon()) && Objects.nonNull(request.getExcludeToOzon())
                && request.getExcludeFromOzon().isAfter(request.getExcludeToOzon())) {
            throw new ValidationException("Начало периода исключения заказов озон больше окончания периода");
        }
        byte[] zip = marketplaceReportsProcessor.getNewPostingsAndGenerateReport(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", ZIP_NAME);

        log.info("Успешно сгенерирован архив по отправлениям на отгрузку по запросу {}", request);
        return new ResponseEntity<>(zip, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping(value = "/orders", produces = EXCEL_MEDIA_TYPE)
    public ResponseEntity<byte[]> generateOrdersReportAfterSearch(@RequestBody SearchOrderRequest request) {
        log.info("Получен запрос на генерацию отчета по заказам");
        var xlsxFile = marketplaceReportsProcessor.searchOrderAndGenerateReport(request);
        if (xlsxFile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String filename = xlsxFile.getFileName();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        // RFC 5987 формат: filename*=UTF-8''<encoded>
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
        headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));

        log.info("Успешно завершена генерация отчета по найденным заказам");
        return ResponseEntity.ok()
                .headers(headers)
                .body(xlsxFile.getContent());
    }

    @PostMapping(value = "/orders/chpu", produces = EXCEL_MEDIA_TYPE)
    public ResponseEntity<byte[]> generateChpuTemplateByOrderNumbers(@RequestBody GenerateChpuTemplateRequest request) {
        log.info("Получен запрос на генерацию ЧПУ шаблонов по заказам: {}", request.getOrderNumbers());
        var xlsxFile = marketplaceReportsProcessor.generateChpuTemplateReport(request.getOrderNumbers());

        String filename = xlsxFile.getFileName();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        // RFC 5987 формат: filename*=UTF-8''<encoded>
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
        headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));

        log.info("Успешно завершена генерация ЧПУ шаблонов");
        return ResponseEntity.ok()
                .headers(headers)
                .body(xlsxFile.getContent());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping(value = "/employees/capacity", produces = EXCEL_MEDIA_TYPE)
    public ResponseEntity<byte[]> generateEmployeesCapacityOrder(@RequestBody EmployeeCapacitySearchRequest request) {
        log.info("Получен запрос на генерацию отчета по выполненной работе: {}", request);
        var xlsxFile = marketplaceReportsProcessor.generateEmployeeCapacityOrder(request);

        if (xlsxFile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String filename = xlsxFile.getFileName();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        // RFC 5987 формат: filename*=UTF-8''<encoded>
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
        headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));

        log.info("Успешно завершена генерация отчета о выполненной работе");
        return ResponseEntity.ok()
                .headers(headers)
                .body(xlsxFile.getContent());
    }
}
