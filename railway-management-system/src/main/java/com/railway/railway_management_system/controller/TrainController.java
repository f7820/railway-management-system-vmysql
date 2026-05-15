package com.railway.railway_management_system.controller;

import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Train;
import com.railway.railway_management_system.response.ApiResponse;
import com.railway.railway_management_system.responseDto.TrainDto;
import com.railway.railway_management_system.service.ITrainService;
import com.railway.railway_management_system.requestDto.AddTrainRequest;
import com.railway.railway_management_system.requestDto.UpdateTrainRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/trains")
public class TrainController {

    private final ITrainService trainService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllTrains() {
        try {
            List<Train> trains = trainService.getAllTrains();
            List<TrainDto> trainDtos = trains.stream()
                    .map(trainService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All trains retrieved successfully", trainDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTrainById(@PathVariable Long id) {
        try {
            Train train = trainService.getTrainById(id);
            return ResponseEntity.ok(new ApiResponse("Train retrieved successfully",
                    trainService.convertToDto(train)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/trainNumber/{trainNumber}")
    public ResponseEntity<ApiResponse> getTrainByTrainNumber(
            @PathVariable String trainNumber) {
        try {
            Train train = trainService.getTrainByTrainNumber(trainNumber);
            return ResponseEntity.ok(new ApiResponse("Train retrieved successfully",
                    trainService.convertToDto(train)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse> getTrainByName(@RequestParam String trainName) {
        try {
            List<Train> trains = trainService.getTrainByName(trainName);
            List<TrainDto> trainDtos = trains.stream()
                    .map(trainService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Trains retrieved successfully", trainDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/type")
    public ResponseEntity<ApiResponse> getTrainByType(@RequestParam String type) {
        try {
            List<Train> trains = trainService.getTrainByType(type);
            List<TrainDto> trainDtos = trains.stream()
                    .map(trainService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Trains retrieved successfully", trainDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addTrain(@RequestBody AddTrainRequest request) {
        try {
            Train train = trainService.addTrain(request);
            return ResponseEntity.ok(new ApiResponse("Train added successfully",
                    trainService.convertToDto(train)));
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
    public ResponseEntity<ApiResponse> updateTrain(
            @RequestBody UpdateTrainRequest updateRequest,
            @PathVariable Long id) {
        try {
            Train train = trainService.updateTrain(updateRequest, id);
            return ResponseEntity.ok(new ApiResponse("Train updated successfully",
                    trainService.convertToDto(train)));
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
    @PutMapping("/update/trainNumber/{trainNumber}")
    public ResponseEntity<ApiResponse> updateTrainByTrainNumber(
            @RequestBody UpdateTrainRequest updateRequest,
            @PathVariable String trainNumber) {
        try {
            Train train = trainService.updateTrainByTrainNumber(updateRequest, trainNumber);
            return ResponseEntity.ok(new ApiResponse("Train updated successfully",
                    trainService.convertToDto(train)));
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
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteTrainById(@PathVariable Long id) {
        try {
            trainService.deleteTrainById(id);
            return ResponseEntity.ok(new ApiResponse("Train deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/trainNumber/{trainNumber}")
    public ResponseEntity<ApiResponse> deleteTrainByTrainNumber(
            @PathVariable String trainNumber) {
        try {
            trainService.deleteTrainByTrainNumber(trainNumber);
            return ResponseEntity.ok(new ApiResponse("Train deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

}
