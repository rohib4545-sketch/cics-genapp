package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Shared policy block (mirrors CA-POLICY-COMMON). */
public record PolicyCommon(
        Integer policyNumber,
        Integer customerNumber,
        LocalDate issueDate,
        LocalDate expiryDate,
        LocalDateTime lastChanged,
        Integer brokerId,
        String brokersReference,
        Integer payment,
        String policyType) {
}
