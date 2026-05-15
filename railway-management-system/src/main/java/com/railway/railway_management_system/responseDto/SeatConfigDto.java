package com.railway.railway_management_system.responseDto;

import com.railway.railway_management_system.enums.SeatClass;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
@Data
public class SeatConfigDto {

   
    private String seatClass;

    private Integer totalSeats;

    private Double basePrice;

    
}
