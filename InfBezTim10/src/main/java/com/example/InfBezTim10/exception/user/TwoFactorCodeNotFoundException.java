package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.NotFoundException;

public class TwoFactorCodeNotFoundException extends NotFoundException {

    public TwoFactorCodeNotFoundException(String message) {
        super(message);
    }
}
