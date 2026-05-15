package com.railway.railway_management_system.controller;

import com.railway.railway_management_system.exceptions.NoSeatsAvailableException;
import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Booking;
import com.railway.railway_management_system.response.ApiResponse;
import com.railway.railway_management_system.responseDto.BookingDto;
import com.railway.railway_management_system.requestDto.AddBookingRequest;
import com.railway.railway_management_system.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/bookings")
public class BookingController {

    private final IBookingService bookingService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            List<BookingDto> bookingDtos = bookings.stream()
                    .map(bookingService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All bookings retrieved successfully", bookingDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(new ApiResponse("Booking retrieved successfully",
                    bookingService.convertToDto(booking)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/bookingReference/{bookingReference}")
    public ResponseEntity<ApiResponse> getBookingByReference(@PathVariable String bookingReference) {
        try {
            Booking booking = bookingService.getBookingByBookingReference(bookingReference);
            return ResponseEntity.ok(new ApiResponse("Booking retrieved successfully",
                    bookingService.convertToDto(booking)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/passengerIdentificationNumber")
    public ResponseEntity<ApiResponse> getBookingsByPassengerIdentificationNumber(@RequestParam Integer passengerIdentificationNumber) {
        try {
            List<Booking> bookings = bookingService.getBookingsByPassengerIdentificationNumber(passengerIdentificationNumber);
            List<BookingDto> bookingDtos = bookings.stream()
                    .map(bookingService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Bookings retrieved successfully", bookingDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/scheduleNumber")
    public ResponseEntity<ApiResponse> getBookingsByScheduleNumber(@RequestParam String scheduleNumber) {
        try {
            List<Booking> bookings = bookingService.getBookingsByScheduleNumber(scheduleNumber);
            List<BookingDto> bookingDtos = bookings.stream()
                    .map(bookingService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Bookings retrieved successfully", bookingDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addBooking(@RequestBody AddBookingRequest request) {
        try {
            Booking booking = bookingService.addBooking(request);
            return ResponseEntity.ok(new ApiResponse("Booking added successfully",
                    bookingService.convertToDto(booking)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (NoSeatsAvailableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse> cancelBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.cancelBookingById(id);
            return ResponseEntity.ok(new ApiResponse("Booking cancelled successfully",
                    bookingService.convertToDto(booking)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PatchMapping("/cancel/bookingReference/{bookingReference}")
    public ResponseEntity<ApiResponse> cancelBookingByReference(@PathVariable String bookingReference) {
        try {
            Booking booking = bookingService.cancelBookingByReference(bookingReference);
            return ResponseEntity.ok(new ApiResponse("Booking cancelled successfully",
                    bookingService.convertToDto(booking)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteBookingById(@PathVariable Long id) {
        try {
            bookingService.deleteBookingById(id);
            return ResponseEntity.ok(new ApiResponse("Booking deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/bookingReference/{bookingReference}")
    public ResponseEntity<ApiResponse> deleteBookingByReference(@PathVariable String bookingReference) {
        try {
            bookingService.deleteBookingByReference(bookingReference);
            return ResponseEntity.ok(new ApiResponse("Booking deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
