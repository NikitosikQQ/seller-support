package ru.seller_support.assignment.controller.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class SignUpRequest {
    String username;
    String password;
}
