package com.railway.railway_management_system.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddBookingRequest {

    @NotBlank
    private String scheduleNumber;  // SCH001, SCH002, ...

    @NotNull
    private Integer passengerIdentificationNumber; //10- digit unique number (1139282677)

    @NotBlank
    private String seatClass;       // "ECONOMY" | "BUSINESS" | "SLEEPER"
}