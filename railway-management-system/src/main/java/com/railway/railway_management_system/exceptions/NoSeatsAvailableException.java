package com.railway.railway_management_system.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String message) { super(message); }
}
