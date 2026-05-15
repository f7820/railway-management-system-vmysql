package com.railway.railway_management_system.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    private long   totalTrains;
    private long   totalRoutes;
    private long   totalSchedules;
    private long   totalCompletedTrips;
    private long   totalBookings;
    private long   totalPassengers;
    private double totalRevenue;
    private double seatOccupancyRate;
}
