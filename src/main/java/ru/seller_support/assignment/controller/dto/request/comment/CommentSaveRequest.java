package ru.seller_support.assignment.controller.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;

@Value
@Builder
@Jacksonized
public class CommentSaveRequest {

    @NotBlank(message = "Не должен быть пустым")
    String value;

    List<ConditionForCommentSaveDto> conditions;

    Set<String> articlesName;

    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    public static class ConditionForCommentSaveDto {
        private String groupLogic;
        private List<RuleForCommentSaveDto> rules;
    }

    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    public static class RuleForCommentSaveDto {
        String field;
        String condition;
        String value;
    }

}
