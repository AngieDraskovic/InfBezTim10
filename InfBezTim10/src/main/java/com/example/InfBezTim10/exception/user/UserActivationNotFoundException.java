package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.NotFoundException;

public class UserActivationNotFoundException extends NotFoundException {
    public UserActivationNotFoundException(String message) {
        super(message);
    }

}
