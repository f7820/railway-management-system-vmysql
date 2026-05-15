package com.railway.railway_management_system.service;

import com.railway.railway_management_system.enums.ScheduleStatus;
import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.*;
import com.railway.railway_management_system.enums.BookingStatus;
import com.railway.railway_management_system.repository.BookingRepository;
import com.railway.railway_management_system.repository.ScheduleRepository;
import com.railway.railway_management_system.repository.ScheduleSeatAvailabilityRepository;
import com.railway.railway_management_system.requestDto.AddScheduleRequest;
import com.railway.railway_management_system.requestDto.UpdateScheduleRequest;
import com.railway.railway_management_system.responseDto.RouteDto;
import com.railway.railway_management_system.responseDto.ScheduleDto;
import com.railway.railway_management_system.responseDto.SeatAvailabilityDto;
import com.railway.railway_management_system.responseDto.TrainDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService implements IScheduleService {

    private final ScheduleRepository                 scheduleRepository;
    private final ScheduleSeatAvailabilityRepository availabilityRepository;
    private final BookingRepository                  bookingRepository;
    private final ITrainService                      trainService;
    private final IRouteService                      routeService;
    private final ModelMapper                        modelMapper;

    // ── queries ───────────────────────────────────────────────────────────────

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAllWithDetails();
    }

    @Override
    public ScheduleDto convertToDto(Schedule schedule) {
        ScheduleDto dto = modelMapper.map(schedule, ScheduleDto.class);
        dto.setCompleted(schedule.isCompleted());
        dto.setStatus(schedule.getStatus().name());
        dto.setTrain(modelMapper.map(schedule.getTrain(), TrainDto.class));
        dto.setRoute(modelMapper.map(schedule.getRoute(), RouteDto.class));
        dto.setSeatAvailabilities(
            schedule.getSeatAvailabilities().stream()
                .map(this::mapAvailabilityToDto)
                .toList()
        );
        return dto;
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found!"));
    }

    @Override
    public Schedule getScheduleByScheduleNumber(String scheduleNumber) {
        return scheduleRepository.findByScheduleNumber(scheduleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found!"));
    }

    @Override
    public List<Schedule> getSchedulesByTrainNumber(String trainNumber) {
        List<Schedule> schedules = scheduleRepository.findByTrain_TrainNumber(trainNumber);
        if (schedules.isEmpty())
            throw new ResourceNotFoundException("No schedules found for train: " + trainNumber);
        return schedules;
    }

    @Override
    public List<Schedule> getSchedulesByRouteNumber(String routeNumber) {
        List<Schedule> schedules = scheduleRepository.findByRoute_RouteNumber(routeNumber);
        if (schedules.isEmpty())
            throw new ResourceNotFoundException("No schedules found for route: " + routeNumber);
        return schedules;
    }

    @Override
    public List<Schedule> getSchedulesByStatus(String status) {
        ScheduleStatus scheduleStatus = ScheduleStatus.valueOf(status);
        List<Schedule> schedules = scheduleRepository.findByStatus(scheduleStatus);
        if (schedules.isEmpty())
            throw new ResourceNotFoundException("No schedules found with status: " + status);
        return schedules;
    }

    /**
     * Returns live seat counts for each class on this schedule.
     * Useful for a dedicated availability endpoint or embedding in ScheduleDto.
     */
    @Override
    public List<SeatAvailabilityDto> getSeatAvailability(String scheduleNumber) {
        Schedule schedule = getScheduleByScheduleNumber(scheduleNumber);
        return availabilityRepository.findBySchedule(schedule)
                .stream()
                .map(this::mapAvailabilityToDto)
                .toList();
    }

    // ── mutations ─────────────────────────────────────────────────────────────

    /**
     * Creates the schedule then seeds one ScheduleSeatAvailability row per seat
     * class, cloned from the train's SeatConfig. This is the single place where
     * availability records are born.
     */
    @Override
    @Transactional
    public Schedule addSchedule(AddScheduleRequest request) {
        Train train = trainService.getTrainByTrainNumber(request.getTrainNumber());
        Route route = routeService.getRouteByRouteNumber(request.getRouteNumber());

        Schedule schedule = new Schedule();
        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setStatus(ScheduleStatus.values()[0]);
        schedule.setTrain(train);
        schedule.setRoute(route);

        schedule.setCompleted(false);
        Schedule saved = scheduleRepository.save(schedule); // triggers @PostPersist → scheduleNumber

        // Seed one availability record per seat class from the train's static config
        List<ScheduleSeatAvailability> availabilities = train.getSeatConfigurations()
                .stream()
                .map(cfg -> buildAvailability(cfg, saved))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // Attach to the returned entity so callers have the full picture in-memory
        saved.setSeatAvailabilities(availabilities);
        return saved;
    }

    @Override
    @Transactional
    public Schedule updateSchedule(UpdateScheduleRequest updateRequest, Long id) {
        Schedule schedule = getScheduleById(id);
        applyUpdates(schedule, updateRequest);
        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public Schedule updateScheduleByScheduleNumber(UpdateScheduleRequest updateRequest,
                                                   String scheduleNumber) {
        Schedule schedule = getScheduleByScheduleNumber(scheduleNumber);
        applyUpdates(schedule, updateRequest);
        return scheduleRepository.save(schedule);
    }

    @Override
    public long countAllSchedules() {
        return scheduleRepository.count();
    }

    @Override
    public long countCompletedSchedules() {
        return scheduleRepository.countByCompletedTrue();
    }

    @Override
    @Transactional
    public Schedule markCompletedById(Long id) {
        Schedule schedule = getScheduleById(id);
        return markCompleted(schedule);
    }

    @Override
    @Transactional
    public Schedule markCompletedByScheduleNumber(String scheduleNumber) {
        Schedule schedule = getScheduleByScheduleNumber(scheduleNumber);
        return markCompleted(schedule);
    }

    /** Runs every minute and auto-completes any non-cancelled schedule whose arrival time has passed. */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void autoCompleteExpiredSchedules() {
        List<Schedule> due = scheduleRepository.findSchedulesToAutoComplete(
                LocalDateTime.now(), ScheduleStatus.CANCELLED);
        if (!due.isEmpty()) {
            due.forEach(s -> s.setCompleted(true));
            scheduleRepository.saveAllAndFlush(due);
            bookingRepository.completeBookingsForSchedules(due, BookingStatus.COMPLETED, BookingStatus.CONFIRMED);
        }
    }

    @Override
    @Transactional
    public void deleteScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found!"));
        bookingRepository.deleteAllBySchedule(schedule);
        scheduleRepository.delete(schedule);
    }

    @Override
    @Transactional
    public void deleteScheduleByScheduleNumber(String scheduleNumber) {
        Schedule schedule = scheduleRepository.findByScheduleNumber(scheduleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found!"));
        bookingRepository.deleteAllBySchedule(schedule);
        scheduleRepository.delete(schedule);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /** Applies non-null fields from the update request to the schedule entity. */
    private void applyUpdates(Schedule schedule, UpdateScheduleRequest req) {
        if (schedule.isCompleted()) {
            throw new IllegalStateException(
                "Schedule " + schedule.getScheduleNumber() +
                " is completed and cannot be modified.");
        }
        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new IllegalStateException(
                "Schedule " + schedule.getScheduleNumber() +
                " is cancelled and cannot be modified.");
        }
        if (req.getDepartureTime() != null) schedule.setDepartureTime(req.getDepartureTime());
        if (req.getArrivalTime()   != null) schedule.setArrivalTime(req.getArrivalTime());
        if (req.getStatus()        != null) {
            ScheduleStatus newStatus = ScheduleStatus.valueOf(req.getStatus());
            schedule.setStatus(newStatus);
            if (newStatus == ScheduleStatus.CANCELLED) {
                bookingRepository.cancelBookingsBySchedule(
                    schedule, BookingStatus.CANCELLED, BookingStatus.CONFIRMED, LocalDateTime.now());
            }
        }
        if (req.getTrainNumber()   != null) {
            Train newTrain = trainService.getTrainByTrainNumber(req.getTrainNumber());
            if (!newTrain.getId().equals(schedule.getTrain().getId())) {
                boolean hasActiveBookings = availabilityRepository.findBySchedule(schedule).stream()
                        .anyMatch(a -> a.getAvailableSeats() < a.getTotalSeats());
                if (hasActiveBookings) {
                    throw new IllegalStateException(
                        "Cannot change the train for schedule " + schedule.getScheduleNumber() +
                        " while it has active bookings.");
                }
                schedule.setTrain(newTrain);
                schedule.getSeatAvailabilities().clear();
                newTrain.getSeatConfigurations().stream()
                        .map(cfg -> buildAvailability(cfg, schedule))
                        .forEach(schedule.getSeatAvailabilities()::add);
            }
        }
        if (req.getRouteNumber()   != null) schedule.setRoute(routeService.getRouteByRouteNumber(req.getRouteNumber()));
        if (req.getCompleted()     != null) {
            if (Boolean.TRUE.equals(req.getCompleted()) && schedule.getStatus() == ScheduleStatus.CANCELLED) {
                throw new IllegalStateException(
                    "Cannot mark a CANCELLED schedule as completed. Only ON_TIME or DELAYED schedules can be completed.");
            }
            schedule.setCompleted(req.getCompleted());
        }
    }

    private Schedule markCompleted(Schedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new IllegalStateException(
                "Cannot mark a CANCELLED schedule as completed. Only ON_TIME or DELAYED schedules can be completed.");
        }
        if (schedule.isCompleted()) {
            throw new IllegalStateException(
                "Schedule " + schedule.getScheduleNumber() + " is already marked as completed.");
        }
        schedule.setCompleted(true);
        // saveAndFlush writes completed=true to the DB immediately. Without this, Hibernate's
        // auto-flush skips the dirty Schedule before the Booking UPDATE runs, and then
        // @Modifying(clearAutomatically=true) evicts the entity from the L1 cache — losing the
        // completed=true change before the transaction commits.
        Schedule saved = scheduleRepository.saveAndFlush(schedule);
        bookingRepository.completeBookingsBySchedule(saved, BookingStatus.COMPLETED, BookingStatus.CONFIRMED);
        // Re-fetch with JOIN FETCH so all lazy associations are initialized before the transaction
        // closes and convertToDto can read them without LazyInitializationException.
        return scheduleRepository.findByScheduleNumberWithDetails(saved.getScheduleNumber()).orElse(saved);
    }

    private ScheduleSeatAvailability buildAvailability(SeatConfig cfg, Schedule schedule) {
        ScheduleSeatAvailability avail = new ScheduleSeatAvailability();
        avail.setSchedule(schedule);
        avail.setSeatClass(cfg.getSeatClass());
        avail.setTotalSeats(cfg.getTotalSeats());
        avail.setAvailableSeats(cfg.getTotalSeats()); // fully open at creation
        avail.setNextSeatNumber(1);                   // first seat will be prefix + "1"
        avail.setBasePrice(cfg.getBasePrice());
        return avail;
    }

    private SeatAvailabilityDto mapAvailabilityToDto(ScheduleSeatAvailability avail) {
        SeatAvailabilityDto dto = new SeatAvailabilityDto();
        dto.setSeatClass(avail.getSeatClass().name());
        dto.setTotalSeats(avail.getTotalSeats());
        dto.setAvailableSeats(avail.getAvailableSeats());
        dto.setBookedSeats(avail.getTotalSeats() - avail.getAvailableSeats());
        dto.setBasePrice(avail.getBasePrice());
        return dto;
    }
}

