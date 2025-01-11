package ru.seller_support.assignment.controller.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class ErrorResponse {

    private String errorCode;
    private String message;

    public ErrorResponse(Exception e) {
        this.errorCode = DEFAULT_ERROR_CODE;
        this.message = e.getMessage();
    }

    private static final String DEFAULT_ERROR_CODE = "ERROR";
}
