package ru.seller_support.assignment.controller.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CreateUserRequest {
    String username;
    String password;
    List<String> roles;
}
