package ru.seller_support.assignment.adapter.marketplace;

import ru.seller_support.assignment.adapter.postgres.entity.ShopEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.time.Instant;
import java.util.List;

public abstract class MarketplaceAdapter {

    public abstract Marketplace getMarketplace();

    public abstract List<PostingInfoModel> getNewPosting(ShopEntity shop, Instant from, Instant to);
}
