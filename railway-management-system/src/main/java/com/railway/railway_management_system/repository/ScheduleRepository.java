package com.railway.railway_management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.railway.railway_management_system.model.Schedule;
import com.railway.railway_management_system.enums.ScheduleStatus;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByScheduleNumber(String scheduleNumber);

    List<Schedule> findByTrain_TrainNumber(String trainNumber);

    List<Schedule> findByRoute_RouteNumber(String routeNumber);

    List<Schedule> findByStatus(ScheduleStatus status);

    long countByCompletedTrue();

    @Query("SELECT s FROM Schedule s WHERE s.completed = false AND s.arrivalTime <= :now AND s.status <> :cancelled")
    List<Schedule> findSchedulesToAutoComplete(@Param("now") LocalDateTime now,
                                               @Param("cancelled") ScheduleStatus cancelled);

    @Query("SELECT DISTINCT s FROM Schedule s " +
           "LEFT JOIN FETCH s.seatAvailabilities " +
           "LEFT JOIN FETCH s.train " +
           "LEFT JOIN FETCH s.route")
    List<Schedule> findAllWithDetails();

    @Query("SELECT s FROM Schedule s " +
           "LEFT JOIN FETCH s.seatAvailabilities " +
           "LEFT JOIN FETCH s.train " +
           "LEFT JOIN FETCH s.route " +
           "WHERE s.scheduleNumber = :scheduleNumber")
    Optional<Schedule> findByScheduleNumberWithDetails(@Param("scheduleNumber") String scheduleNumber);
}
