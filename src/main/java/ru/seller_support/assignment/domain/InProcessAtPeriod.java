package ru.seller_support.assignment.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
public class InProcessAtPeriod {
    private LocalDateTime fromInProcessAt;
    private LocalDateTime toInProcessAt;
}
