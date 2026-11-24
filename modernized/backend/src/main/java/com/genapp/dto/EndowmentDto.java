package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EndowmentDto(
        String withProfits,
        String equities,
        String managedFund,
        String fundName,
        Integer term,
        Integer sumAssured,
        String lifeAssured,
        String paddingData) {
}
