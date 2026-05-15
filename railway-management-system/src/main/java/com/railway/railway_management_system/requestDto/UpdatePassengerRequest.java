package com.railway.railway_management_system.requestDto;

import com.railway.railway_management_system.enums.Gender;
import lombok.Data;

@Data
public class UpdatePassengerRequest {
    private Integer identificationNumber;

    private String firstName;

    private String lastName;

    private String gender;

    private String email;

    private String phoneNumber;
}
