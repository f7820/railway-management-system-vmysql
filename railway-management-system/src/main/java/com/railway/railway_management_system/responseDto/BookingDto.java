package com.railway.railway_management_system.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDto {
   
    private String bookingReference;
    private String seatNumber;
    private String bookedClass;
    private Double finalPrice;
    private String status;
    private LocalDateTime bookingDate;
    private LocalDateTime cancelDate;

    // Flattened passenger snapshot — avoids a full nested PassengerDto
    private Integer passengerIdentificationNumber;
    private String passengerName;

    // Flattened schedule snapshot
    private String scheduleNumber;
    private String trainNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
}
