package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CommercialDto(
        LocalDateTime requestDate,
        LocalDate startDate,
        LocalDate renewalDate,
        String address,
        String zipcode,
        String latitudeN,
        String longitudeW,
        String customer,
        String propertyType,
        Integer firePeril,
        Integer firePremium,
        Integer crimePeril,
        Integer crimePremium,
        Integer floodPeril,
        Integer floodPremium,
        Integer weatherPeril,
        Integer weatherPremium,
        Integer status,
        String rejectionReason) {
}
