package ru.seller_support.assignment.service.comment.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.comment.RuleForCommentModel;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopNameConditionForCommentHandler implements ConditionForCommentHandler {

    @Override
    public FieldForCondition getField() {
        return FieldForCondition.SHOP_NAME;
    }

    @Override
    public boolean checkConditions(RuleForCommentModel rule, PostingInfoModel postingInfo) {
        if (Objects.isNull(rule)) {
            return true;
        }
        ConditionOperator operator = rule.getCondition();
        String ruleValue = rule.getValue().toLowerCase();
        String actualValue = postingInfo.getShopName().toLowerCase();

        if (!ConditionOperator.OPERATORS_FOR_STRING_AND_BOOLEAN.contains(operator)) {
            return false;
        }

        return compareValues(actualValue, ruleValue, operator);
    }
}