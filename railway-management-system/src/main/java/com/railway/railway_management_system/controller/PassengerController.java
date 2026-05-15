package com.railway.railway_management_system.controller;

import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Passenger;
import com.railway.railway_management_system.response.ApiResponse;
import com.railway.railway_management_system.responseDto.PassengerDto;
import com.railway.railway_management_system.service.IPassengerService;
import com.railway.railway_management_system.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.railway.railway_management_system.requestDto.AddPassengerRequest;
import com.railway.railway_management_system.requestDto.UpdatePassengerRequest;


import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/passengers")
public class PassengerController {

    private final IPassengerService passengerService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllPassengers() {
        try {
            List<Passenger> passengers = passengerService.getAllPassengers();
            List<PassengerDto> passengerDtos = passengers.stream()
                    .map(passengerService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All passengers retrieved successfully", passengerDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPassengerById(@PathVariable Long id) {
        try {
            Passenger passenger = passengerService.getPassengerById(id);
            return ResponseEntity.ok(new ApiResponse("Passenger retrieved successfully",
                    passengerService.convertToDto(passenger)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/identification/{identificationNumber}")
    public ResponseEntity<ApiResponse> getPassengerByIdentificationNumber(
            @PathVariable Integer identificationNumber) {
        try {
            Passenger passenger = passengerService.getPassengerByIdentificationNumber(identificationNumber);
            return ResponseEntity.ok(new ApiResponse("Passenger retrieved successfully",
                    passengerService.convertToDto(passenger)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse> getPassengerByFirstNameAndLastName(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        try {
            List<Passenger> passengers = passengerService.getPassengerByFirstNameAndLastName(firstName, lastName);
            List<PassengerDto> passengerDtos = passengers.stream()
                    .map(passengerService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Passengers retrieved successfully", passengerDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/email")
    public ResponseEntity<ApiResponse> getPassengerByEmail(@RequestParam String email) {
        try {
            List<Passenger> passengers = passengerService.getPassengerByEmail(email);
            List<PassengerDto> passengerDtos = passengers.stream()
                    .map(passengerService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Passengers retrieved successfully", passengerDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse> getPassengerByPhoneNumber(@RequestParam String phoneNumber) {
        try {
            List<Passenger> passengers = passengerService.getPassengerByPhoneNumber(phoneNumber);
            List<PassengerDto> passengerDtos = passengers.stream()
                    .map(passengerService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Passengers retrieved successfully", passengerDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addPassenger(@RequestBody AddPassengerRequest request) {
        try {
            Passenger passenger = passengerService.addPassenger(request);
            return ResponseEntity.ok(new ApiResponse("Passenger added successfully",
                    passengerService.convertToDto(passenger)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid value: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/update/{identificationNumber}")
    public ResponseEntity<ApiResponse> updatePassenger(
            @RequestBody UpdatePassengerRequest updateRequest,
            @PathVariable Integer identificationNumber) {
        try {
            Passenger passenger = passengerService.updatePassenger(updateRequest, identificationNumber);
            return ResponseEntity.ok(new ApiResponse("Passenger updated successfully",
                    passengerService.convertToDto(passenger)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/id/{id}")
    public ResponseEntity<ApiResponse> deletePassengerById(@PathVariable Long id) {
        try {
            passengerService.deletePassengerById(id);
            return ResponseEntity.ok(new ApiResponse("Passenger deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/identification/{identificationNumber}")
    public ResponseEntity<ApiResponse> deletePassengerByIdentificationNumber(
            @PathVariable Integer identificationNumber) {
        try {
            passengerService.deletePassengerByIdentificationNumber(identificationNumber);
            return ResponseEntity.ok(new ApiResponse("Passenger deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

}
