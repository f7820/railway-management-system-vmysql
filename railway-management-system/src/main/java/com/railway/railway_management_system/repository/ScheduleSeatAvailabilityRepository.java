package com.railway.railway_management_system.repository;

import com.railway.railway_management_system.enums.SeatClass;
import com.railway.railway_management_system.model.Schedule;
import com.railway.railway_management_system.model.ScheduleSeatAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ScheduleSeatAvailabilityRepository
        extends JpaRepository<ScheduleSeatAvailability, Long> {

    List<ScheduleSeatAvailability> findBySchedule(Schedule schedule);

    /**
     * SELECT ... FOR UPDATE — blocks concurrent seat assignments for the same
     * schedule + class until the current transaction commits.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ScheduleSeatAvailability a " +
           "WHERE a.schedule = :schedule AND a.seatClass = :seatClass")
    Optional<ScheduleSeatAvailability> findByScheduleAndSeatClassForUpdate(
            @Param("schedule") Schedule schedule,
            @Param("seatClass") SeatClass seatClass);

    @Query("SELECT COALESCE(SUM(a.totalSeats), 0) FROM ScheduleSeatAvailability a WHERE a.schedule.completed = true")
    Long sumTotalSeats();

    @Query("SELECT COALESCE(SUM(a.totalSeats - a.availableSeats), 0) FROM ScheduleSeatAvailability a WHERE a.schedule.completed = true")
    Long sumBookedSeats();

    @Query("SELECT a.schedule.train.trainNumber, a.schedule.train.name, " +
           "COALESCE(SUM(a.totalSeats), 0), COALESCE(SUM(a.totalSeats - a.availableSeats), 0) " +
           "FROM ScheduleSeatAvailability a " +
           "WHERE a.schedule.completed = true " +
           "GROUP BY a.schedule.train.trainNumber, a.schedule.train.name")
    List<Object[]> findTrainSeatFillRate();
}
