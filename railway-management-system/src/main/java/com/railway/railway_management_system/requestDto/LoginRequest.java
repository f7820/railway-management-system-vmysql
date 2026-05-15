package com.railway.railway_management_system.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Invalid credentials")
    private String username;

    @NotBlank(message = "Invalid credentials")
    private String password;
}