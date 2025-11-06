package ru.seller_support.assignment.controller.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class CommentUpdateRequest {

    @NotNull(message = "Не должен отсутствовать")
    UUID id;

    @NotBlank(message = "Не должен быть пустым")
    String value;

    List<CommentSaveRequest.ConditionForCommentSaveDto> conditions;

    Set<String> articlesName;

}
