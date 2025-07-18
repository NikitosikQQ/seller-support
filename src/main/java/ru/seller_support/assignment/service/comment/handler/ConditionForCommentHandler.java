package ru.seller_support.assignment.service.comment.handler;

import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.comment.ConditionForCommentModel;
import ru.seller_support.assignment.domain.comment.RuleForCommentModel;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;
import ru.seller_support.assignment.domain.enums.GroupLogic;

import java.util.Objects;

public interface ConditionForCommentHandler {

    String SEPARATOR_OF_COMMENTS = ", ";
    String FORMAT_FOR_NEW_COMMENT = "%s%s%s";

    FieldForCondition getField();

    boolean checkConditions(RuleForCommentModel rule, PostingInfoModel postingInfoModel);

    @SuppressWarnings("unchecked")
    default boolean compareValues(Object actual, Object expected, ConditionOperator operator) {
        if (actual instanceof Comparable && expected instanceof Comparable) {
            Comparable<Object> actualComp = (Comparable<Object>) actual;
            Comparable<Object> expectedComp = (Comparable<Object>) expected;

            int result = actualComp.compareTo(expectedComp);

            return switch (operator) {
                case EQ -> result == 0;
                case NE -> result != 0;
                case GT -> result > 0;
                case GTE -> result >= 0;
                case LT -> result < 0;
                case LTE -> result <= 0;
            };
        } else {
            // Некомпарируемые значения — fallback через equals
            return operator == ConditionOperator.EQ && actual.equals(expected)
                    || operator == ConditionOperator.NE && !actual.equals(expected);
        }
    }

    default String prepareComment(String currentComment, String commentValue, boolean resultOfCheckConditions) {
        return resultOfCheckConditions
                ? currentComment.isEmpty()
                ? commentValue
                : String.format(FORMAT_FOR_NEW_COMMENT, currentComment, SEPARATOR_OF_COMMENTS, commentValue)
                : currentComment;
    }

    default boolean checkPassedWithGroupLogic(ConditionForCommentModel conditionForComment, int passedCount) {
        GroupLogic groupLogic = conditionForComment.getGroupLogic();
        if (Objects.isNull(groupLogic)) {
            return false;
        }
        return switch (groupLogic) {
            case AND -> passedCount == conditionForComment.getRules().size();
            case OR, NOTHING -> passedCount > 0;
            case XOR -> passedCount == 1;
        };
    }
}



