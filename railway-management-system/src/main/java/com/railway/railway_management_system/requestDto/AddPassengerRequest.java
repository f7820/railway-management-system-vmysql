package com.railway.railway_management_system.requestDto;

import com.railway.railway_management_system.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class AddPassengerRequest {

    private Integer identificationNumber;

    private String firstName;

    private String lastName;

    private String gender;

    private String email;

    private String phoneNumber;

}
