package com.railway.railway_management_system.service;

import com.railway.railway_management_system.model.Schedule;
import com.railway.railway_management_system.requestDto.AddScheduleRequest;
import com.railway.railway_management_system.requestDto.UpdateScheduleRequest;
import com.railway.railway_management_system.responseDto.ScheduleDto;
import com.railway.railway_management_system.responseDto.SeatAvailabilityDto;

import java.util.List;

public interface IScheduleService {
    List<Schedule> getAllSchedules();
    ScheduleDto convertToDto(Schedule schedule);
    Schedule getScheduleById(Long id);
    Schedule getScheduleByScheduleNumber(String scheduleNumber);
    List<Schedule> getSchedulesByTrainNumber(String trainNumber);
    List<Schedule> getSchedulesByRouteNumber(String routeNumber);
    List<Schedule> getSchedulesByStatus(String status);

    List<SeatAvailabilityDto> getSeatAvailability(String scheduleNumber);

    long countAllSchedules();
    long countCompletedSchedules();

    Schedule addSchedule(AddScheduleRequest request);
    Schedule updateSchedule(UpdateScheduleRequest updateRequest, Long id);
    Schedule updateScheduleByScheduleNumber(UpdateScheduleRequest updateRequest, String scheduleNumber);
    Schedule markCompletedById(Long id);
    Schedule markCompletedByScheduleNumber(String scheduleNumber);
    void deleteScheduleById(Long id);
    void deleteScheduleByScheduleNumber(String scheduleNumber);
}
