package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.NotFoundException;

public class PasswordResetNotFoundException extends NotFoundException {
    public PasswordResetNotFoundException(String message) {
        super(message);
    }

}
