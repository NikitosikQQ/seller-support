package ru.seller_support.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.service.MarketplaceProcessor;
import ru.seller_support.assignment.util.CommonUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private static final String ZIP_FILE_PATTERN = "Postings%s.zip";

    private final MarketplaceProcessor marketplaceProcessor;

    @GetMapping(value = "/posting", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> test(@RequestParam("from") String from,
                                       @RequestParam("to") String to) {
        Instant now = Instant.now();
        byte[] zip = marketplaceProcessor.getNewPostings(from, to, now);
        String zipName = CommonUtils.getFormattedStringWithInstant(ZIP_FILE_PATTERN, now);
        String encodedZipName = URLEncoder.encode(zipName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", encodedZipName);

        return new ResponseEntity<>(zip, headers, HttpStatus.OK);
    }
}
