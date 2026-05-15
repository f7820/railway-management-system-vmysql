package com.railway.railway_management_system.controller;

import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Schedule;
import com.railway.railway_management_system.response.ApiResponse;
import com.railway.railway_management_system.responseDto.ScheduleDto;
import com.railway.railway_management_system.responseDto.SeatAvailabilityDto;
import com.railway.railway_management_system.requestDto.AddScheduleRequest;
import com.railway.railway_management_system.requestDto.UpdateScheduleRequest;
import com.railway.railway_management_system.service.IScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/schedules")
public class ScheduleController {

    private final IScheduleService scheduleService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllSchedules() {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            List<ScheduleDto> scheduleDtos = schedules.stream()
                    .map(scheduleService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All schedules retrieved successfully", scheduleDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getScheduleById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id);
            return ResponseEntity.ok(new ApiResponse("Schedule retrieved successfully",
                    scheduleService.convertToDto(schedule)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/scheduleNumber/{scheduleNumber}")
    public ResponseEntity<ApiResponse> getScheduleByScheduleNumber(@PathVariable String scheduleNumber) {
        try {
            Schedule schedule = scheduleService.getScheduleByScheduleNumber(scheduleNumber);
            return ResponseEntity.ok(new ApiResponse("Schedule retrieved successfully",
                    scheduleService.convertToDto(schedule)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/trainNumber")
    public ResponseEntity<ApiResponse> getSchedulesByTrainNumber(@RequestParam String trainNumber) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByTrainNumber(trainNumber);
            List<ScheduleDto> scheduleDtos = schedules.stream()
                    .map(scheduleService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Schedules retrieved successfully", scheduleDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/routeNumber")
    public ResponseEntity<ApiResponse> getSchedulesByRouteNumber(@RequestParam String routeNumber) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByRouteNumber(routeNumber);
            List<ScheduleDto> scheduleDtos = schedules.stream()
                    .map(scheduleService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Schedules retrieved successfully", scheduleDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/status")
    public ResponseEntity<ApiResponse> getSchedulesByStatus(@RequestParam String status) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByStatus(status);
            List<ScheduleDto> scheduleDtos = schedules.stream()
                    .map(scheduleService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Schedules retrieved successfully", scheduleDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/availability/{scheduleNumber}")
    public ResponseEntity<ApiResponse> getSeatAvailability(@PathVariable String scheduleNumber) {
        try {
            Schedule schedule = scheduleService.getScheduleByScheduleNumber(scheduleNumber);
            List<SeatAvailabilityDto> availability = scheduleService.getSeatAvailability(scheduleNumber);
            return ResponseEntity.ok(new ApiResponse(
                    "Seat availability retrieved successfully",
                    Map.of("completed", schedule.isCompleted(),
                           "status",    schedule.getStatus().name(),
                           "availability", availability)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getScheduleStats() {
        return ResponseEntity.ok(new ApiResponse("Schedule stats retrieved successfully",
                java.util.Map.of(
                        "totalSchedules",    scheduleService.countAllSchedules(),
                        "totalCompletedTrips", scheduleService.countCompletedSchedules())));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMINISTRATOR')")
    @PostMapping("/complete/{id}")
    public ResponseEntity<ApiResponse> markScheduleCompletedById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.markCompletedById(id);
            return ResponseEntity.ok(new ApiResponse("Schedule marked as completed successfully",
                    scheduleService.convertToDto(schedule)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMINISTRATOR')")
    @PostMapping("/complete/scheduleNumber/{scheduleNumber}")
    public ResponseEntity<ApiResponse> markScheduleCompletedByScheduleNumber(@PathVariable String scheduleNumber) {
        try {
            Schedule schedule = scheduleService.markCompletedByScheduleNumber(scheduleNumber);
            return ResponseEntity.ok(new ApiResponse("Schedule marked as completed successfully",
                    scheduleService.convertToDto(schedule)));
        }
          catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addSchedule(@RequestBody AddScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.addSchedule(request);
            return ResponseEntity.ok(new ApiResponse("Schedule added successfully",
                    scheduleService.convertToDto(schedule)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid value: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateSchedule(
            @RequestBody UpdateScheduleRequest updateRequest,
            @PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.updateSchedule(updateRequest, id);
            return ResponseEntity.ok(new ApiResponse("Schedule updated successfully",
                    scheduleService.convertToDto(schedule)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PutMapping("/update/scheduleNumber/{scheduleNumber}")
    public ResponseEntity<ApiResponse> updateScheduleByScheduleNumber(
            @RequestBody UpdateScheduleRequest updateRequest,
            @PathVariable String scheduleNumber) {
        try {
            Schedule schedule = scheduleService.updateScheduleByScheduleNumber(updateRequest, scheduleNumber);
            return ResponseEntity.ok(new ApiResponse("Schedule updated successfully",
                    scheduleService.convertToDto(schedule)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteScheduleById(@PathVariable Long id) {
        try {
            scheduleService.deleteScheduleById(id);
            return ResponseEntity.ok(new ApiResponse("Schedule deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/scheduleNumber/{scheduleNumber}")
    public ResponseEntity<ApiResponse> deleteScheduleByScheduleNumber(
            @PathVariable String scheduleNumber) {
        try {
            scheduleService.deleteScheduleByScheduleNumber(scheduleNumber);
            return ResponseEntity.ok(new ApiResponse("Schedule deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
