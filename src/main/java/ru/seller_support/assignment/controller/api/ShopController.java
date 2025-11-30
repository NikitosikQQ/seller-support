package ru.seller_support.assignment.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.shop.CreateShopRequest;
import ru.seller_support.assignment.controller.dto.request.shop.DeleteShopRequest;
import ru.seller_support.assignment.controller.dto.request.shop.ShopChangeRequest;
import ru.seller_support.assignment.controller.dto.response.ShopResponse;
import ru.seller_support.assignment.service.ShopService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ShopController {

    private final ShopService shopService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShopResponse> getAllShops() {
        return shopService.findAll().stream()
                .map(shop -> ShopResponse.builder()
                        .id(shop.getId())
                        .name(shop.getName())
                        .marketplace(shop.getMarketplace())
                        .palletNumber(shop.getPalletNumber())
                        .build())
                .toList();
    }

    @GetMapping(path = "/{marketplace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShopResponse> getAllShopsByMarketplace(@PathVariable("marketplace") String marketplaceName) {
        return shopService.findAllByMarketplace(marketplaceName).stream()
                .map(shop -> ShopResponse.builder()
                        .id(shop.getId())
                        .name(shop.getName())
                        .marketplace(shop.getMarketplace())
                        .palletNumber(shop.getPalletNumber())
                        .build())
                .toList();
    }

    @GetMapping(path = "/marketplaces", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getMarketplaceNames() {
        return shopService.getAllMarketplaces();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createShop(@RequestBody @Valid CreateShopRequest request) {
        shopService.saveShop(request);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateShop(@RequestBody @Valid ShopChangeRequest request) {
        shopService.updateShop(request);
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteShop(@RequestBody @Valid DeleteShopRequest request) {
        shopService.delete(request);
    }

}
