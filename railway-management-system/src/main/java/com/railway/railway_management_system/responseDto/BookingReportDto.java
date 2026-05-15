package com.railway.railway_management_system.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingReportDto {
    private long   completedBookings;
    private long   confirmedBookings;
    private long   cancelledBookings;
    private long   totalBookings;
    private double revenue;
}
