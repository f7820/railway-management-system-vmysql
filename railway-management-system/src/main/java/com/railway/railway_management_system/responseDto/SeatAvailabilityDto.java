package com.railway.railway_management_system.responseDto;



import lombok.Data;

@Data
public class SeatAvailabilityDto {
    private String  seatClass;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer bookedSeats;
    private Double  basePrice;
}
