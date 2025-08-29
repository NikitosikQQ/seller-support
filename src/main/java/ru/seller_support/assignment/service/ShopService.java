package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.adapter.postgres.repository.ShopRepository;
import ru.seller_support.assignment.controller.dto.request.CreateShopRequest;
import ru.seller_support.assignment.controller.dto.request.DeleteShopRequest;
import ru.seller_support.assignment.controller.dto.request.ShopChangeRequest;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.exception.ShopChangeException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final TextEncryptService encryptService;
    private final ShopRepository shopRepository;

    @Transactional
    public void saveShop(CreateShopRequest request) {
        ShopEntity shop = new ShopEntity();
        String encryptedApiKey = encryptService.encrypt(request.getApiKey());

        shop.setName(request.getName());
        shop.setClientId(request.getClientId());
        shop.setMarketplace(request.getMarketplace());
        shop.setPalletNumber(request.getPalletNumber());
        shop.setApiKey(encryptedApiKey);
        shop.setActive(true);
        shop.setBusinessId(request.getBusinessId());
        shopRepository.save(shop);
    }

    @Transactional
    public List<ShopEntity> findAllByMarketplace(String marketplaceName) {
        Marketplace marketplace = Marketplace.valueOf(marketplaceName);
        return shopRepository.findAllByMarketplace(marketplace);
    }

    @Transactional(readOnly = true)
    public List<ShopEntity> findAll() {
        return shopRepository.findAllByActive(true);
    }

    @Transactional(readOnly = true)
    public List<ShopEntity> findAllByMarketplaceActive(Marketplace marketplace) {
        return shopRepository.findAllByMarketplaceAndActive(marketplace, true);
    }

    public List<String> getAllMarketplaces() {
        return Arrays.stream(Marketplace.values())
                .map(Marketplace::toString)
                .toList();
    }

    @Transactional
    public void delete(DeleteShopRequest request) {
        shopRepository.deleteById(request.getId());
    }

    @Transactional
    public void updateShop(ShopChangeRequest request) {
        ShopEntity shop = shopRepository.findById(request.getId())
                .orElseThrow(() ->
                        new ShopChangeException(String.format("Магазин с id %s не найден", request.getId())));
        if (Objects.nonNull(request.getName())) {
            shop.setName(request.getName());
        }
        if (Objects.nonNull(request.getMarketplace())) {
            shop.setMarketplace(request.getMarketplace());
        }
        if (Objects.nonNull(request.getPalletNumber())) {
            shop.setPalletNumber(request.getPalletNumber());
        }
        if (Objects.nonNull(request.getClientId())) {
            shop.setClientId(request.getClientId());
        }
        if (Objects.nonNull(request.getBusinessId())) {
            shop.setBusinessId(request.getBusinessId());
        }
        if (Objects.nonNull(request.getApiKey())) {
            shop.setApiKey(encryptService.encrypt(request.getApiKey()));
        }
        shopRepository.save(shop);
    }

}
