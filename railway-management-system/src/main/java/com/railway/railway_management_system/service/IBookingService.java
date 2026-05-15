package com.railway.railway_management_system.service;

import com.railway.railway_management_system.model.Booking;
import com.railway.railway_management_system.requestDto.AddBookingRequest;
import com.railway.railway_management_system.requestDto.UpdateBookingRequest;
import com.railway.railway_management_system.responseDto.BookingDto;

import java.util.List;

public interface IBookingService {

    List<Booking> getAllBookings();
    BookingDto    convertToDto(Booking booking);
    Booking       getBookingById(Long id);
    Booking       getBookingByBookingReference(String bookingReference);
    List<Booking> getBookingsByPassengerIdentificationNumber(Integer passengerIdentificationNumber); //changed!!!!!!!!
    List<Booking> getBookingsByScheduleNumber(String scheduleNumber);

    Booking addBooking(AddBookingRequest request);
    Booking cancelBookingById(Long id);
    Booking cancelBookingByReference(String bookingReference);
    void    deleteBookingById(Long id);
    void    deleteBookingByReference(String bookingReference);
}
