package com.railway.railway_management_system.requestDto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.railway.railway_management_system.enums.ScheduleStatus;

import lombok.Data;

@Data
public class UpdateScheduleRequest {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;

    private String status;

    private String trainNumber;

    private String routeNumber;

    private Boolean completed;

}