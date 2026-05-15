package com.railway.railway_management_system.responseDto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.railway.railway_management_system.enums.ScheduleStatus;
import com.railway.railway_management_system.model.Train;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Data;
@Data
public class ScheduleDto {

    private String scheduleNumber; // i.e(SCH009)

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;

    private String status;

    private boolean completed;

    private TrainDto train;

    private RouteDto route;

    private List<SeatAvailabilityDto> seatAvailabilities;

    
}
