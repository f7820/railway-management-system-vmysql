package com.railway.railway_management_system.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainRevenueDto {
    private String trainNumber;
    private String trainName;
    private String trainType;
    private double revenue;
    private long   completedBookings;
    private long   completedTrips;
}
