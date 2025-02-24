package ru.seller_support.assignment.adapter.marketplace.yandexmarket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexStickerDownloader {

    private final RestTemplate restTemplate;

    public byte[] downloadStickersFromUrl(String url) {
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        ResponseEntity<byte[]> response = restTemplate.getForEntity(decodedUrl, byte[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException(response.getStatusCode().toString());
        }
    }
}
