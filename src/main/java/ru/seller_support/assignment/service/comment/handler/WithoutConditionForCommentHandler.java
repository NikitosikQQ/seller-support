package ru.seller_support.assignment.service.comment.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.comment.RuleForCommentModel;
import ru.seller_support.assignment.domain.enums.FieldForCondition;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithoutConditionForCommentHandler implements ConditionForCommentHandler {

    @Override
    public FieldForCondition getField() {
        return FieldForCondition.WITHOUT_CONDITIONS;
    }

    @Override
    public boolean checkConditions(RuleForCommentModel rule, PostingInfoModel postingInfo) {
        return true;
    }
}