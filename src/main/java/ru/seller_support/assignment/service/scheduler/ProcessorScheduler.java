package ru.seller_support.assignment.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.service.order.OrderStatusRefresher;
import ru.seller_support.assignment.service.processor.CleanUpProcessor;
import ru.seller_support.assignment.service.processor.MarketplaceCollectPostingsProcessor;
import ru.seller_support.assignment.service.processor.MarketplaceImportOrdersProcessor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorScheduler {

    private final MarketplaceCollectPostingsProcessor collectPostingsProcessor;
    private final MarketplaceImportOrdersProcessor getPostingsProcessor;
    private final OrderStatusRefresher orderStatusRefresher;
    private final CleanUpProcessor cleanUpProcessor;

    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Moscow")
    //@Scheduled(cron = "0 */2 * * * *", zone = "Europe/Moscow")
    public void processGetOrders() {
        log.info("Старт сборки заказов из маркетплейса для отгрузки");
        collectPostingsProcessor.processCollectPostings();
        log.info("Успешно завершена сборка заказов из маркетплейса");
//        log.info("Авто-старт получения заказов из маркетплейсов");
//        getPostingsProcessor.importNewPostings(null);
//        log.info("Автоматическое получение новых заказы из маркетплейсов успешно завершено");
    }

    @Scheduled(cron = "0 0 * * * MON", zone = "Europe/Moscow")
    //@Scheduled(cron = "0 */2 * * * *", zone = "Europe/Moscow")
    public void processCleanUp() {
        log.info("Старт очистки старых сделанных заказов");
        int count = cleanUpProcessor.cleanUpOrders();
        log.info("Очистка сделанных заказов произошла успешно, удалено {} заказов", count);

        log.info("Старт очистки старых выполненных работ");
        cleanUpProcessor.cleanEmployeeCapacity();
        log.info("Очиста старых выполненных работ произошла успешно");
    }

    @Scheduled(cron = "0 0 20 * * *", zone = "Europe/Moscow")
    public void processRefreshOrderStatuses() {
        orderStatusRefresher.actualizeWildberriesOrderStatus();
        orderStatusRefresher.refreshOrdersStatus();
    }
}
