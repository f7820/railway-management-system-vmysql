package com.railway.railway_management_system.service;

import com.railway.railway_management_system.enums.Gender;
import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Passenger;
import com.railway.railway_management_system.repository.PassengerRepository;
import com.railway.railway_management_system.requestDto.AddPassengerRequest;
import com.railway.railway_management_system.requestDto.UpdatePassengerRequest;
import com.railway.railway_management_system.responseDto.PassengerDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PassengerService implements IPassengerService{

    private final PassengerRepository passengerRepository;
    private final ModelMapper modelMapper;

    @Override
    public Passenger getPassengerById(Long Id) {
        return passengerRepository.findById(Id).orElseThrow(() -> new ResourceNotFoundException("Passenger not found!"));
    }

    @Override
    public Passenger getPassengerByIdentificationNumber(Integer identificationNumber) {
        return passengerRepository.findByIdentificationNumber(identificationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found!"));
    }

    @Override
    public List<Passenger> getPassengerByFirstNameAndLastName(String firstName, String lastName) {
        List<Passenger> passengers = passengerRepository.findByFirstNameAndLastName(firstName, lastName);
        if (passengers.isEmpty()) {
            throw new ResourceNotFoundException("No passengers found with name: " + firstName + " " + lastName);
        }
        return passengers;
    }

    @Override
    public List<Passenger> getPassengerByEmail(String email) {
        List<Passenger> passengers = passengerRepository.findByEmail(email);
        if (passengers.isEmpty()) {
            throw new ResourceNotFoundException("No passengers found with email: " + email);
        }
        return passengers;
    }

    @Override
    public List<Passenger> getPassengerByPhoneNumber(String phoneNumber) {
        List<Passenger> passengers = passengerRepository.findByPhoneNumber(phoneNumber);
        if (passengers.isEmpty()) {
            throw new ResourceNotFoundException("No passengers found with phone number: " + phoneNumber);
        }
        return passengers;
    }

    @Override
    public List<Passenger> getAllPassengers() {
        List<Passenger> passengers = passengerRepository.findAll();
        if (passengers.isEmpty()) {
            throw new ResourceNotFoundException("No passengers found!");
        }
        return passengers;
    }

    @Override
    public Passenger addPassenger(AddPassengerRequest passenger) {
        return passengerRepository.save(createPassenger(passenger));
    }

    @Override
    public Passenger createPassenger(AddPassengerRequest passenger) {
        return new Passenger(passenger.getIdentificationNumber(),passenger.getFirstName(),
                passenger.getLastName(), Gender.valueOf(passenger.getGender()),passenger.getEmail(),passenger.getPhoneNumber());
    }


    @Override
    public Passenger updatePassenger(UpdatePassengerRequest updateRequest, Integer identificationNumber) {


        Optional<Passenger> passengerOptional = passengerRepository.findByIdentificationNumber(identificationNumber);

        if(passengerOptional.isPresent()) {
            Passenger passenger = passengerOptional.get();
            passenger.setIdentificationNumber(updateRequest.getIdentificationNumber());
            passenger.setFirstName(updateRequest.getFirstName());
            passenger.setLastName(updateRequest.getLastName());
            passenger.setGender(Gender.valueOf(updateRequest.getGender()));
            passenger.setEmail(updateRequest.getEmail());
            passenger.setPhoneNumber(updateRequest.getPhoneNumber());
            return passengerRepository.save(passenger);
        }
        else {
            throw new ResourceNotFoundException("Passenger not found!");
        }

    }

    @Override
    public void deletePassengerById(Long id) {
        passengerRepository.findById(id)
                .ifPresentOrElse(passengerRepository::delete,
                        () -> {throw new ResourceNotFoundException("Passenger not found!");});
    }

    @Override
    public void deletePassengerByIdentificationNumber(Integer identificationNumber) {
        passengerRepository.findByIdentificationNumber(identificationNumber)
                .ifPresentOrElse(passengerRepository::delete,
                        () -> {throw new ResourceNotFoundException("Passenger not found!");});

    }

    @Override
    public PassengerDto convertToDto(Passenger passenger) {
        return modelMapper.map(passenger, PassengerDto.class);
    }
}
