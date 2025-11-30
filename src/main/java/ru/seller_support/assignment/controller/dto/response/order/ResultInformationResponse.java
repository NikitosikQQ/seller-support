package ru.seller_support.assignment.controller.dto.response.order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ResultInformationResponse {
    private String message;
    private Boolean orderWasUpdated;
    private Boolean needAlert;
}
