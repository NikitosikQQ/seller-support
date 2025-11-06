package ru.seller_support.assignment.controller.dto.request.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CommentDeleteRequest {

    @NotNull(message = "не должно быть пустым")
    UUID id;
}
