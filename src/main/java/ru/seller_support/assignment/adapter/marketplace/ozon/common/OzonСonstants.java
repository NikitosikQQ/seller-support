package ru.seller_support.assignment.adapter.marketplace.ozon.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OzonСonstants {

    public static final String ASC_SORT = "ASC";
    public static final String DESC_SORT = "DESC";
    public static final Integer MAX_LIMIT = 1000;

    @UtilityClass
    public static class OzonStatus {
        public static String AWAITING_DELIVER = "awaiting_deliver";
        public static String ACCEPTANCE_IN_PROGRESS = "acceptance_in_progress";
    }

}
