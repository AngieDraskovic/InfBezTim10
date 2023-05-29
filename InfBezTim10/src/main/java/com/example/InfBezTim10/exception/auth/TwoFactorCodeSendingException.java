package com.example.InfBezTim10.exception.auth;

import com.example.InfBezTim10.exception.CustomException;

public class TwoFactorCodeSendingException extends CustomException {
    public TwoFactorCodeSendingException(String message) {
        super(message);
    }
}
