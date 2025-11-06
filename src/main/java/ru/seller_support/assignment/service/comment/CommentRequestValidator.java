package ru.seller_support.assignment.service.comment;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest;
import ru.seller_support.assignment.controller.dto.request.comment.CommentUpdateRequest;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;
import ru.seller_support.assignment.domain.enums.GroupLogic;

import java.util.List;
import java.util.Objects;

import static ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest.ConditionForCommentSaveDto;
import static ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest.RuleForCommentSaveDto;

@Service
public class CommentRequestValidator {

    public void validate(CommentSaveRequest request) {
        ConditionForCommentSaveDto condition = request.getConditions().getFirst();
        baseValidate(condition);
    }

    public void validate(CommentUpdateRequest request) {
        ConditionForCommentSaveDto condition = request.getConditions().getFirst();
        baseValidate(condition);
    }

    private void baseValidate(ConditionForCommentSaveDto condition) {
        String groupLogicValue = condition.getGroupLogic();
        GroupLogic groupLogic = GroupLogic.fromSymbol(groupLogicValue);
        List<RuleForCommentSaveDto> rules = condition.getRules();

        if ((Objects.isNull(rules) || rules.isEmpty() || rules.size() < 2) && groupLogic != GroupLogic.NOTHING) {
            throw new ValidationException("Некорректно указан тип логической группировки. " +
                    "Либо укажи тип логической группировки \"БЕЗ ГРУППИРОВКИ\", либо добавь более 1 простого условия");
        }
        rules.forEach(rule -> {
            ConditionOperator operator = ConditionOperator.fromSymbol(rule.getCondition());
            FieldForCondition field = FieldForCondition.fromSymbol(rule.getField());
            String value = rule.getValue();
            if (Objects.isNull(field) || Objects.isNull(operator) || StringUtils.isEmpty(value)) {
                throw new ValidationException("В одном из условий не указано поле для сравнения или оператор сравнения или значение условия. " +
                        "Укажи все поля полностью или удали условие");
            }
        });
    }
}
