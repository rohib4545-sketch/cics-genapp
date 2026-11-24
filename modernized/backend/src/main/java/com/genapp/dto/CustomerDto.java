package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Customer payload (mirrors CA-CUSTOMER-REQUEST). */
public record CustomerDto(
        Integer customerNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String houseName,
        String houseNumber,
        String postcode,
        String phoneMobile,
        String phoneHome,
        String emailAddress) {
}
