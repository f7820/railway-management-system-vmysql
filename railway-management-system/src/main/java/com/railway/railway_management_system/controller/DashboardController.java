package com.railway.railway_management_system.controller;

import com.railway.railway_management_system.response.ApiResponse;
import com.railway.railway_management_system.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/dashboard")
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getDashboardStats() {
        return ResponseEntity.ok(new ApiResponse(
                "Dashboard statistics retrieved successfully",
                dashboardService.getStats()));
    }

    @GetMapping("/train-seat-fill-rate")
    public ResponseEntity<ApiResponse> getTrainSeatFillRate() {
        return ResponseEntity.ok(new ApiResponse(
                "Train seat fill rate retrieved successfully",
                dashboardService.getTrainSeatFillRate()));
    }

    @GetMapping("/revenue-by-train")
    public ResponseEntity<ApiResponse> getRevenueByTrain() {
        return ResponseEntity.ok(new ApiResponse(
                "Revenue by train retrieved successfully",
                dashboardService.getRevenueByTrain()));
    }

    @GetMapping("/daily-booking-report")
    public ResponseEntity<ApiResponse> getDailyBookingReport() {
        return ResponseEntity.ok(new ApiResponse(
                "Daily booking report retrieved successfully",
                dashboardService.getDailyBookingReport()));
    }

    @GetMapping("/monthly-booking-report")
    public ResponseEntity<ApiResponse> getMonthlyBookingReport() {
        return ResponseEntity.ok(new ApiResponse(
                "Monthly booking report retrieved successfully",
                dashboardService.getMonthlyBookingReport()));
    }
}
