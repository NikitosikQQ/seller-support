package ru.seller_support.assignment.domain.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;

@Data
@AllArgsConstructor
public class RuleForCommentModel {

    FieldForCondition field;
    ConditionOperator condition;
    String value;
}
