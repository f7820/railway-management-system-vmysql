package com.railway.railway_management_system.responseDto;

import java.util.List;

import com.railway.railway_management_system.enums.TrainType;
import com.railway.railway_management_system.model.SeatConfig;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
public class TrainDto {
 
    private String trainNumber;

    private String name;

    private String type;

    
    private List<SeatConfigDto> seatConfigurations;
    
}
