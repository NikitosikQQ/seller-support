package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UserChangeRequest {

    @NotBlank(message = "не должно быть пустым")
    String username;

    String updatedUsername;
    List<String> updatedRoles;
}
