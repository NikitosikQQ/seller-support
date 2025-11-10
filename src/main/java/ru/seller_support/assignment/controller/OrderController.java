package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.order.ImportOrdersRequest;
import ru.seller_support.assignment.controller.dto.request.order.OrderHistoryDto;
import ru.seller_support.assignment.controller.dto.request.order.SearchChpuOrderRequest;
import ru.seller_support.assignment.controller.dto.request.order.SearchOrderRequest;
import ru.seller_support.assignment.controller.dto.response.ChpuOrderDto;
import ru.seller_support.assignment.controller.dto.response.order.OrderDto;
import ru.seller_support.assignment.service.mapper.OrderMapper;
import ru.seller_support.assignment.service.order.OrderPackageService;
import ru.seller_support.assignment.service.order.OrderSearchService;
import ru.seller_support.assignment.service.processor.MarketplaceImportOrdersProcessor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final MarketplaceImportOrdersProcessor importOrdersProcessor;
    private final OrderPackageService orderPackageService;
    private final OrderSearchService orderSearchService;

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
}
