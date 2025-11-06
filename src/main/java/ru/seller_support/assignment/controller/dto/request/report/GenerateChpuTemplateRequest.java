package ru.seller_support.assignment.controller.dto.request.report;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class GenerateChpuTemplateRequest {

    @NotEmpty
    List<String> orderNumbers;
}
