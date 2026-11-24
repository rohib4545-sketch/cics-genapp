package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MotorDto(
        String make,
        String model,
        Integer value,
        String regNumber,
        String colour,
        Integer cc,
        LocalDate yearOfManufacture,
        Integer premium,
        Integer accidents) {
}
