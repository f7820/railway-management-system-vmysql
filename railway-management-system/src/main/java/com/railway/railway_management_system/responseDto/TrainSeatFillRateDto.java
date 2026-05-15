package com.railway.railway_management_system.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainSeatFillRateDto {
    private String trainNumber;
    private String trainName;
    private long   totalSeats;
    private long   bookedSeats;
    private double fillRate;
}
