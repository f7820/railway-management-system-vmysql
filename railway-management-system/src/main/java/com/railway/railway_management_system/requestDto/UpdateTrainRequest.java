package com.railway.railway_management_system.requestDto;

import java.util.List;

import com.railway.railway_management_system.responseDto.SeatConfigDto;

import lombok.Data;

@Data
public class UpdateTrainRequest {
    
    private String name;

    private String type;
    
    private List<SeatConfigDto> seatConfigurations;
    
}
