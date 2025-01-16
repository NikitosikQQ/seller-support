package ru.seller_support.assignment.controller.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class DeleteUserRequest {
    String username;
}
