package com.railway.railway_management_system.repository;

import com.railway.railway_management_system.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface PassengerRepository extends JpaRepository<Passenger,Long> {

    Optional<Passenger> findByIdentificationNumber(Integer identificationNumber);

    List<Passenger> findByEmail(String email);

    List<Passenger> findByPhoneNumber(String phoneNumber);

    List<Passenger> findByFirstNameAndLastName(String firstName, String lastName);

}
