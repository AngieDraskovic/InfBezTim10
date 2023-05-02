package com.example.InfBezTim10.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordDoNotMatchException extends Exception{
    public PasswordDoNotMatchException(String message) {
        super(message);
    }
}
