package com.railway.railway_management_system.requestDto;



import lombok.Data;

@Data
public class UpdateBookingRequest {
    // Bookings are mostly immutable after confirmation.
    // The primary mutable field is status (e.g. CANCELLED).
    private String status;
}
