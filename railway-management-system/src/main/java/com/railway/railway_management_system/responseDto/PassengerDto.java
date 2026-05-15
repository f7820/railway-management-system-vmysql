package com.railway.railway_management_system.responseDto;

import com.railway.railway_management_system.enums.Gender;
import lombok.Data;


@Data
public class PassengerDto {
    private Integer identificationNumber;

    private String firstName;

    private String lastName;

    private String gender;

    private String email;

    private String phoneNumber;
}
