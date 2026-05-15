package com.railway.railway_management_system.service;

import com.railway.railway_management_system.responseDto.BookingReportDto;
import com.railway.railway_management_system.responseDto.DashboardStatsDto;
import com.railway.railway_management_system.responseDto.TrainRevenueDto;
import com.railway.railway_management_system.responseDto.TrainSeatFillRateDto;

import java.util.List;

public interface IDashboardService {
    DashboardStatsDto getStats();
    List<TrainSeatFillRateDto> getTrainSeatFillRate();
    List<TrainRevenueDto> getRevenueByTrain();
    BookingReportDto getDailyBookingReport();
    BookingReportDto getMonthlyBookingReport();
}
