package ru.seller_support.assignment.domain.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.seller_support.assignment.domain.enums.GroupLogic;

import java.util.List;

@Data
@AllArgsConstructor
public class ConditionForCommentModel {

    private GroupLogic groupLogic;
    private List<RuleForCommentModel> rules;

}
