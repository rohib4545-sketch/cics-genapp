package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Read response: common block + the typed detail block. */
public record PolicyResponse(
        PolicyCommon common,
        EndowmentDto endowment,
        HouseDto house,
        MotorDto motor,
        CommercialDto commercial) {
}
