package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.CustomException;

public class EmailAlreadyExistsException extends CustomException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
