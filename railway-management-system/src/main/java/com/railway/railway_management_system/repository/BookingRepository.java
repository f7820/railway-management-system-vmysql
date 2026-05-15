package com.railway.railway_management_system.repository;


import com.railway.railway_management_system.enums.BookingStatus;
import com.railway.railway_management_system.model.Booking;
import com.railway.railway_management_system.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    List<Booking>     findByPassenger_IdentificationNumber(Integer identificationNumber);
    List<Booking>     findBySchedule_ScheduleNumber(String scheduleNumber);

    /** Bulk-transitions all CONFIRMED bookings on a schedule to COMPLETED. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Booking b SET b.status = :completed WHERE b.schedule = :schedule AND b.status = :confirmed")
    int completeBookingsBySchedule(@Param("schedule") Schedule schedule,
                                   @Param("completed") BookingStatus completed,
                                   @Param("confirmed") BookingStatus confirmed);

    /** Bulk-transitions all CONFIRMED bookings on a schedule to CANCELLED, recording the cancel date. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Booking b SET b.status = :cancelled, b.cancelDate = :cancelDate WHERE b.schedule = :schedule AND b.status = :confirmed")
    int cancelBookingsBySchedule(@Param("schedule") Schedule schedule,
                                 @Param("cancelled") BookingStatus cancelled,
                                 @Param("confirmed") BookingStatus confirmed,
                                 @Param("cancelDate") LocalDateTime cancelDate);

    /** Deletes all bookings for a schedule (used before schedule deletion to satisfy FK constraints). */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Booking b WHERE b.schedule = :schedule")
    void deleteAllBySchedule(@Param("schedule") Schedule schedule);

    /** Bulk-transitions CONFIRMED bookings across a list of schedules to COMPLETED. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Booking b SET b.status = :completed WHERE b.schedule IN :schedules AND b.status = :confirmed")
    int completeBookingsForSchedules(@Param("schedules") List<Schedule> schedules,
                                     @Param("completed") BookingStatus completed,
                                     @Param("confirmed") BookingStatus confirmed);

    /** Returns the sum of finalPrice for all bookings with the given status (0.0 when none). */
    @Query("SELECT COALESCE(SUM(b.finalPrice), 0.0) FROM Booking b WHERE b.status = :status")
    Double sumFinalPriceByStatus(@Param("status") BookingStatus status);

    @Query("SELECT b.schedule.train.trainNumber, b.schedule.train.name, b.schedule.train.type, " +
           "COALESCE(SUM(b.finalPrice), 0.0), COUNT(b), COUNT(DISTINCT b.schedule) " +
           "FROM Booking b " +
           "WHERE b.status = :status " +
           "GROUP BY b.schedule.train.trainNumber, b.schedule.train.name, b.schedule.train.type")
    List<Object[]> findRevenueByTrain(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.schedule.arrivalTime >= :from AND b.schedule.arrivalTime < :to")
    long countCompletedByArrivalTimeRange(@Param("status") BookingStatus status,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.bookingDate >= :from AND b.bookingDate < :to")
    long countConfirmedByBookingDateRange(@Param("status") BookingStatus status,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.cancelDate >= :from AND b.cancelDate < :to")
    long countCancelledByCancelDateRange(@Param("status") BookingStatus status,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(b.finalPrice), 0.0) FROM Booking b WHERE b.status = :status AND b.schedule.arrivalTime >= :from AND b.schedule.arrivalTime < :to")
    double sumRevenueByArrivalTimeRange(@Param("status") BookingStatus status,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);
}
