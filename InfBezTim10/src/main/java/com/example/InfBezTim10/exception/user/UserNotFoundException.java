package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
