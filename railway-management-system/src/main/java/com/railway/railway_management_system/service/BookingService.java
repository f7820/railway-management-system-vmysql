package com.railway.railway_management_system.service;

import com.railway.railway_management_system.enums.BookingStatus;
import com.railway.railway_management_system.enums.SeatClass;
import com.railway.railway_management_system.enums.ScheduleStatus;
import com.railway.railway_management_system.exceptions.NoSeatsAvailableException;
import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.*;
import com.railway.railway_management_system.repository.BookingRepository;
import com.railway.railway_management_system.repository.PassengerRepository;
import com.railway.railway_management_system.repository.ScheduleSeatAvailabilityRepository;
import com.railway.railway_management_system.requestDto.AddBookingRequest;
import com.railway.railway_management_system.responseDto.BookingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    private final BookingRepository                  bookingRepository;
    private final ScheduleSeatAvailabilityRepository availabilityRepository;
    private final IScheduleService                   scheduleService;
    private final PassengerRepository                passengerRepository;

    // ── queries ───────────────────────────────────────────────────────────────

    @Override
    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "bookingDate"));
        if (bookings.isEmpty()) throw new ResourceNotFoundException("No bookings found!");
        return bookings;
    }

    @Override
    public BookingDto convertToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        
        dto.setBookingReference(booking.getBookingReference());
        dto.setSeatNumber(booking.getSeatNumber());
        dto.setBookedClass(booking.getBookedClass().name());
        dto.setFinalPrice(booking.getFinalPrice());
        dto.setStatus(booking.getStatus().name());
        dto.setBookingDate(booking.getBookingDate());
        dto.setCancelDate(booking.getCancelDate());

        // Passenger snapshot
        dto.setPassengerIdentificationNumber(booking.getPassenger().getIdentificationNumber());
        dto.setPassengerName(booking.getPassenger().getFirstName()
                + " " + booking.getPassenger().getLastName());

        // Schedule snapshot
        dto.setScheduleNumber(booking.getSchedule().getScheduleNumber());
        dto.setTrainNumber(booking.getSchedule().getTrain().getTrainNumber());
        dto.setDepartureTime(booking.getSchedule().getDepartureTime());
        dto.setArrivalTime(booking.getSchedule().getArrivalTime());

        return dto;
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
    }

    @Override
    public Booking getBookingByBookingReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
    }

    @Override
    public List<Booking> getBookingsByPassengerIdentificationNumber(Integer passengerIdentificationNumber) {
        List<Booking> bookings = bookingRepository.findByPassenger_IdentificationNumber(passengerIdentificationNumber);
        if (bookings.isEmpty())
            throw new ResourceNotFoundException("No bookings found for passenger: " + passengerIdentificationNumber);
        return bookings;
    }

    @Override
    public List<Booking> getBookingsByScheduleNumber(String scheduleNumber) {
        List<Booking> bookings = bookingRepository.findBySchedule_ScheduleNumber(scheduleNumber);
        if (bookings.isEmpty())
            throw new ResourceNotFoundException("No bookings found for schedule: " + scheduleNumber);
        return bookings;
    }

    // ── mutations ─────────────────────────────────────────────────────────────

    /**
     * Books one seat. Steps:
     *  1. Validate schedule is open for booking.
     *  2. Acquire a pessimistic write lock on the availability row → prevents double-booking.
     *  3. Decrement availableSeats, increment nextSeatNumber.
     *  4. Persist the booking — @PrePersist fills bookingReference, bookingDate, status.
     */
    @Override
    @Transactional
    public Booking addBooking(AddBookingRequest request) {
        Schedule schedule = scheduleService.getScheduleByScheduleNumber(request.getScheduleNumber());

        if (schedule.isCompleted()) {
            throw new IllegalStateException(
                "Schedule " + request.getScheduleNumber() +
                " has already been completed and is no longer accepting bookings.");
        }

        if (schedule.getStatus() != ScheduleStatus.ON_TIME) {
            throw new IllegalStateException(
                "Schedule " + request.getScheduleNumber() + " is not open for booking.");
        }

        Passenger passenger = passengerRepository.findByIdentificationNumber(request.getPassengerIdentificationNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Passenger not found: " + request.getPassengerIdentificationNumber()));

        SeatClass seatClass = SeatClass.valueOf(request.getSeatClass().toUpperCase());

        // Pessimistic lock — only one thread assigns this class's next seat at a time
        ScheduleSeatAvailability availability = availabilityRepository
                .findByScheduleAndSeatClassForUpdate(schedule, seatClass)
                .orElseThrow(() -> new ResourceNotFoundException(
                    seatClass + " class is not configured for schedule " + request.getScheduleNumber()));

        if (availability.getAvailableSeats() <= 0) {
            throw new NoSeatsAvailableException(
                "No " + seatClass + " seats available on " + request.getScheduleNumber());
        }

        // Assign seat — e.g. "E1", "B4", "S12"
        String seatNumber = seatClass.getPrefix() + availability.getNextSeatNumber();

        availability.setNextSeatNumber(availability.getNextSeatNumber() + 1);
        availability.setAvailableSeats(availability.getAvailableSeats() - 1);
        availabilityRepository.save(availability);

        Booking booking = new Booking();
        booking.setSeatNumber(seatNumber);
        booking.setFinalPrice(availability.getBasePrice());
        booking.setBookedClass(seatClass);
        booking.setPassenger(passenger);
        booking.setSchedule(schedule);
        // bookingReference, bookingDate, status → set by @PrePersist

        return bookingRepository.save(booking);
    }

    /**
     * Cancels a booking and returns the seat to the available pool.
     * Note: nextSeatNumber is NOT rolled back — seat numbers are never reused.
     */
    @Override
    @Transactional
    public Booking cancelBookingById(Long id) {
        Booking booking = getBookingById(id);
        return cancelBooking(booking);
    }

    @Override
    @Transactional
    public Booking cancelBookingByReference(String bookingReference) {
        Booking booking = getBookingByBookingReference(bookingReference);
        return cancelBooking(booking);
    }

    @Override
    @Transactional
    public void deleteBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
        restoreSeatIfConfirmed(booking);
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional
    public void deleteBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
        restoreSeatIfConfirmed(booking);
        bookingRepository.delete(booking);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void restoreSeatIfConfirmed(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            ScheduleSeatAvailability availability = availabilityRepository
                    .findByScheduleAndSeatClassForUpdate(booking.getSchedule(), booking.getBookedClass())
                    .orElseThrow(() -> new ResourceNotFoundException("Availability record not found."));
            availability.setAvailableSeats(availability.getAvailableSeats() + 1);
            availabilityRepository.save(availability);
        }
    }

    private Booking cancelBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                "Only CONFIRMED bookings can be cancelled. Current status: " + booking.getStatus());
        }

        // Return the seat to the pool (with pessimistic lock)
        ScheduleSeatAvailability availability = availabilityRepository
                .findByScheduleAndSeatClassForUpdate(booking.getSchedule(), booking.getBookedClass())
                .orElseThrow(() -> new ResourceNotFoundException("Availability record not found."));

        availability.setAvailableSeats(availability.getAvailableSeats() + 1);
        availabilityRepository.save(availability);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelDate(LocalDateTime.now());
        return bookingRepository.save(booking);
    }
}
