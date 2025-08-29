package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceProcessorScheduler {

    private final MarketplaceCollectPostingsProcessor collectPostingsProcessor;

    @Scheduled(cron = "0 0 20,21 * * *", zone = "Europe/Moscow")
    public void processCollectPosting() {
        log.info("Старт сборки заказов из маркетплейса для отгрузки");
        collectPostingsProcessor.processCollectPostings();
    }
}
