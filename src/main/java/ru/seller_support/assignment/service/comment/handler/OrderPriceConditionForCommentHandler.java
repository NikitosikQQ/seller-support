package ru.seller_support.assignment.service.comment.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.comment.RuleForCommentModel;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPriceConditionForCommentHandler implements ConditionForCommentHandler {

    @Override
    public FieldForCondition getField() {
        return FieldForCondition.ORDER_PRICE;
    }


    @Override
    public boolean checkConditions(RuleForCommentModel rule, PostingInfoModel postingInfo) {
        if (Objects.isNull(rule)) {
            return true;
        }
        ConditionOperator operator = rule.getCondition();
        String value = rule.getValue();
        BigDecimal ruleValue = new BigDecimal(value);
        BigDecimal actualValue = postingInfo.getProduct().getTotalPrice();

        if (Objects.isNull(actualValue)) {
            return false;
        }

        return compareValues(actualValue, ruleValue, operator);
    }
}