package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Write request: common block + one typed detail block. */
public record PolicyWriteRequest(
        Integer customerNumber,
        LocalDate issueDate,
        LocalDate expiryDate,
        Integer brokerId,
        String brokersReference,
        Integer payment,
        EndowmentDto endowment,
        HouseDto house,
        MotorDto motor,
        CommercialDto commercial) {
}
