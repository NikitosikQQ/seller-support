package ru.seller_support.assignment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.service.MarketplaceProcessor;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private static final String ZIP_NAME = "Postings";

    private final MarketplaceProcessor marketplaceProcessor;

    @GetMapping(value = "/postings", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> test(@RequestParam("from") String from,
                                       @RequestParam("to") String to,
                                       @RequestParam(value = "supplyId", required = false) String supplyId) {
        log.info("Получен запрос на генерацию файлов по отправлениям на отгрузку от {} до {}", from, to);
        Instant now = Instant.now();
        byte[] zip = marketplaceProcessor.getNewPostings(from, to, now, supplyId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", ZIP_NAME);

        log.info("Успешно сгенерирован архив по отправлениям на отгрузку от {} до {}", from, to);
        return new ResponseEntity<>(zip, headers, HttpStatus.OK);
    }
}
