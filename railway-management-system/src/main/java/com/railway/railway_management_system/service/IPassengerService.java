package com.railway.railway_management_system.service;

import com.railway.railway_management_system.model.Passenger;
import com.railway.railway_management_system.requestDto.AddPassengerRequest;
import com.railway.railway_management_system.requestDto.UpdatePassengerRequest;
import com.railway.railway_management_system.responseDto.PassengerDto;

import java.util.List;

public interface IPassengerService {
    Passenger getPassengerById(Long Id);
    Passenger getPassengerByIdentificationNumber(Integer identificationNumber);
    List<Passenger> getPassengerByFirstNameAndLastName(String firstName,String LastName);
    List<Passenger> getPassengerByEmail(String email);
    List<Passenger> getPassengerByPhoneNumber(String phoneNumber);
    List<Passenger> getAllPassengers();
    Passenger addPassenger(AddPassengerRequest passenger);
    Passenger createPassenger(AddPassengerRequest passenger);
    Passenger updatePassenger(UpdatePassengerRequest updateRequest, Integer identificationNumber);
    void deletePassengerById(Long id);
    void deletePassengerByIdentificationNumber(Integer identificationNumber);
    PassengerDto convertToDto(Passenger passenger);

}
