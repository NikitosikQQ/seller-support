package ru.seller_support.assignment.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.order.*;
import ru.seller_support.assignment.controller.dto.response.ChpuOrderDto;
import ru.seller_support.assignment.controller.dto.response.order.OrderDto;
import ru.seller_support.assignment.controller.dto.response.order.ResultInformationResponse;
import ru.seller_support.assignment.service.mapper.OrderMapper;
import ru.seller_support.assignment.service.order.OrderPackageService;
import ru.seller_support.assignment.service.order.OrderSearchService;
import ru.seller_support.assignment.service.order.OrderService;
import ru.seller_support.assignment.service.order.OrderValidationService;
import ru.seller_support.assignment.service.processor.MarketplaceImportOrdersProcessor;
import ru.seller_support.assignment.util.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final MarketplaceImportOrdersProcessor importOrdersProcessor;
    private final OrderPackageService orderPackageService;
    private final OrderSearchService orderSearchService;
    private final OrderValidationService orderValidationService;
    private final OrderService orderService;

    private final OrderMapper orderMapper;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @PostMapping(path = "/search")
    public ResponseEntity<Page<OrderDto>> searchOrders(@RequestBody @Valid SearchOrderRequest request) {
        log.info("Старт процесса поиска заказов: {}", request);
        var orders = orderSearchService.search(request);
        var response = orders.map(orderMapper::toDto);
        log.info("Успешно завершен поиск заказов");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping(path = "/{number}/history")
    public ResponseEntity<List<OrderHistoryDto>> getOrderHistory(@PathVariable String number) {
        var history = orderSearchService.getHistoryByOrderNumber(number);
        var response = history.stream().map(orderMapper::toHistoryDto).toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @PostMapping(path = "/validation")
    public ResponseEntity<ResultInformationResponse> validateOrderByNumber(@RequestBody VerifyOrderRequest request) {
        log.info("Старт валидации заказа перед его обработкой : {}", request);
        var result = orderValidationService.validateOrder(request);
        log.info("Валидация заказа выполнена: {}, результат: {}", request, result);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping(path = "/status")
    public ResponseEntity<Void> updateStatus(@RequestBody UpdateOrderStatusRequest request) {
        log.info("Старт обновления статуса заказа: {}", request);
        var author = SecurityUtils.getCurrentUsername();
        var orderNumber = request.getNumber();
        var newStatus = request.getNewStatus();
        orderService.updateStatus(orderNumber, newStatus, author);
        log.info("Обновление статуса заказа orderNumber успешно завершено");
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @PostMapping(path = "/chpu/search")
    public ResponseEntity<List<ChpuOrderDto>> searchOrdersForChpu(@RequestBody SearchChpuOrderRequest request) {
        log.info("Старт процесса поиска заказов для ЧПУ: {}", request);
        var orders = orderSearchService.searchChpu(request);
        log.info("Успешно завершен поиск заказов для ЧПУ");
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping(path = "/import")
    public ResponseEntity<Void> importOrders(@RequestBody ImportOrdersRequest request) {
        log.info("Старт процесса ручной загрузки заказов с МП: {}", request);
        importOrdersProcessor.importNewPostings(request);
        log.info("Успешно завершен процесс ручной загрузки заказов с МП: {}", request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @PostMapping(path = "/packages", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPackages(@RequestParam Boolean onlyPackagingMaterials) {
        log.info("Старт процесса скачивания этикеток");
        var pdfBytes = orderPackageService.downloadOrderPackages(onlyPackagingMaterials);
        if (pdfBytes == null) {
            log.info("Не найдены заказы для упаковки");
            return ResponseEntity.notFound().build();
        }
        log.info("Успешно завершен процесс скачивания этикеток");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=packages.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @GetMapping(path = "/{orderNumber}/package", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPackageByOrderNumber(@PathVariable String orderNumber) {
        log.info("Старт процесса скачивания этикетки по номеру {}", orderNumber);
        var pdfBytes = orderPackageService.downloadPackageByOrderNumber(orderNumber);
        if (pdfBytes == null) {
            log.info("Не удалось найти этикетку для заказа по номеру {}", orderNumber);
            return ResponseEntity.notFound().build();
        }
        log.info("Успешно завершен процесс скачивания этикеток");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=packages.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
