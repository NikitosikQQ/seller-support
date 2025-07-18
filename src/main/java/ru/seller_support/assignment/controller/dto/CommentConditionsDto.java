package ru.seller_support.assignment.controller.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class CommentConditionsDto {

    private List<String> logicGroupValues;
    private List<String> conditionOperators;
    private List<String> conditionFields;

}
