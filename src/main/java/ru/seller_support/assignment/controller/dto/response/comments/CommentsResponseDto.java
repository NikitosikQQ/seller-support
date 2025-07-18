package ru.seller_support.assignment.controller.dto.response.comments;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class CommentsResponseDto {

    private UUID id;
    private List<ConditionForCommentDto> conditions;
    private String value;
    private Set<String> articlesName;

    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    public static class ConditionForCommentDto {
        private String groupLogic;
        private List<RuleForCommentDto> rules;
    }

    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    public static class RuleForCommentDto {
        String field;
        String condition;
        String value;
    }
}
