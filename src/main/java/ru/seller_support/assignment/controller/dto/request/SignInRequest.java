package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class SignInRequest {

    @NotBlank(message = "не должно быть пустым")
    String username;

    @NotBlank(message = "не должно быть пустым")
    String password;
}
