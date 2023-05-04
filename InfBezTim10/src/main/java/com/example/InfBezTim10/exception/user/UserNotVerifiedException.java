package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.CustomException;

public class UserNotVerifiedException extends CustomException {
    public UserNotVerifiedException(String message) {
        super(message);
    }
}
