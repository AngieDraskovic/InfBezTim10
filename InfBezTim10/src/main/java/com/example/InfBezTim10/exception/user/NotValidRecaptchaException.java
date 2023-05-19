package com.example.InfBezTim10.exception.user;

import com.example.InfBezTim10.exception.CustomException;

public class NotValidRecaptchaException extends CustomException {
    public NotValidRecaptchaException(String message) {
        super(message);
    }
}
