package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CreateUserRequest {

    @NotBlank(message = "не должно быть пустым")
    @Size(max = 32, message = "не должно превышать 32 символа")
    String username;

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 6, message = "не должно быть меньше 6 символов")
    String password;

    @NotEmpty(message = "список ролей не должен быть пустым")
    List<String> roles;
}
