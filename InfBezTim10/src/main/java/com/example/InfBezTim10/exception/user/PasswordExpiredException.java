package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.CustomException;

public class PasswordExpiredException extends CustomException {

    public PasswordExpiredException(String message) {
        super(message);
    }
}
