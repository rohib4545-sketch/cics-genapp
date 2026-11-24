package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HouseDto(
        String propertyType,
        Integer bedrooms,
        Integer value,
        String houseName,
        String houseNumber,
        String postcode) {
}
