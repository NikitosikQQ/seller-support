package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class UserResponse {

    String id;
    String username;
    List<String> roles;
    List<String> workplaces;
}
