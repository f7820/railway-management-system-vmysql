package com.railway.railway_management_system.service;

import com.railway.railway_management_system.enums.BookingStatus;
import com.railway.railway_management_system.enums.TrainType;
import com.railway.railway_management_system.repository.BookingRepository;
import com.railway.railway_management_system.repository.PassengerRepository;
import com.railway.railway_management_system.repository.RouteRepository;
import com.railway.railway_management_system.repository.ScheduleRepository;
import com.railway.railway_management_system.repository.ScheduleSeatAvailabilityRepository;
import com.railway.railway_management_system.repository.TrainRepository;
import com.railway.railway_management_system.responseDto.BookingReportDto;
import com.railway.railway_management_system.responseDto.DashboardStatsDto;
import com.railway.railway_management_system.responseDto.TrainRevenueDto;
import com.railway.railway_management_system.responseDto.TrainSeatFillRateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final TrainRepository                    trainRepository;
    private final RouteRepository                    routeRepository;
    private final ScheduleRepository                 scheduleRepository;
    private final BookingRepository                  bookingRepository;
    private final PassengerRepository                passengerRepository;
    private final ScheduleSeatAvailabilityRepository availabilityRepository;

    @Override
    public DashboardStatsDto getStats() {
        long totalSeats  = availabilityRepository.sumTotalSeats();
        long bookedSeats = availabilityRepository.sumBookedSeats();
        double occupancyRate = totalSeats == 0 ? 0.0 : bookedSeats * 100.0 / totalSeats;

        return new DashboardStatsDto(
                trainRepository.count(),
                routeRepository.count(),
                scheduleRepository.count(),
                scheduleRepository.countByCompletedTrue(),
                bookingRepository.count(),
                passengerRepository.count(),
                bookingRepository.sumFinalPriceByStatus(BookingStatus.COMPLETED),
                occupancyRate
        );
    }

    @Override
    public List<TrainSeatFillRateDto> getTrainSeatFillRate() {
        return availabilityRepository.findTrainSeatFillRate().stream()
                .map(row -> {
                    String trainNumber = (String) row[0];
                    String trainName   = (String) row[1];
                    long   totalSeats  = ((Number) row[2]).longValue();
                    long   bookedSeats = ((Number) row[3]).longValue();
                    double rate        = totalSeats == 0 ? 0.0 : bookedSeats * 100.0 / totalSeats;
                    return new TrainSeatFillRateDto(trainNumber, trainName, totalSeats, bookedSeats, rate);
                })
                .sorted(Comparator.comparingDouble(TrainSeatFillRateDto::getFillRate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainRevenueDto> getRevenueByTrain() {
        return bookingRepository.findRevenueByTrain(BookingStatus.COMPLETED).stream()
                .map(row -> {
                    String trainNumber     = (String) row[0];
                    String trainName       = (String) row[1];
                    String trainType       = ((TrainType) row[2]).name();
                    double revenue            = ((Number) row[3]).doubleValue();
                    long   completedBookings  = ((Number) row[4]).longValue();
                    long   completedTrips     = ((Number) row[5]).longValue();
                    return new TrainRevenueDto(trainNumber, trainName, trainType, revenue, completedBookings, completedTrips);
                })
                .sorted(Comparator.comparingDouble(TrainRevenueDto::getRevenue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public BookingReportDto getDailyBookingReport() {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to   = from.plusDays(1);
        return buildReport(from, to);
    }

    @Override
    public BookingReportDto getMonthlyBookingReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime to   = from.plusMonths(1);
        return buildReport(from, to);
    }

    private BookingReportDto buildReport(LocalDateTime from, LocalDateTime to) {
        long completed  = bookingRepository.countCompletedByArrivalTimeRange(BookingStatus.COMPLETED, from, to);
        long confirmed  = bookingRepository.countConfirmedByBookingDateRange(BookingStatus.CONFIRMED, from, to);
        long cancelled  = bookingRepository.countCancelledByCancelDateRange(BookingStatus.CANCELLED, from, to);
        long total      = completed + confirmed + cancelled;
        double revenue  = bookingRepository.sumRevenueByArrivalTimeRange(BookingStatus.COMPLETED, from, to);
        return new BookingReportDto(completed, confirmed, cancelled, total, revenue);
    }
}
