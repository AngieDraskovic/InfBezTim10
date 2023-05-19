package com.example.InfBezTim10.exception.certificate;

import com.example.InfBezTim10.exception.NotFoundException;

public class PrivateKeyNotFoundException extends NotFoundException {
    public PrivateKeyNotFoundException(String message) {
        super(message);
    }
}
