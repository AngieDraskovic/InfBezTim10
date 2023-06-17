package com.example.InfBezTim10.exception.certificate;

import com.example.InfBezTim10.exception.CustomException;

public class PrivateKeyReadException extends CustomException {
    public PrivateKeyReadException(String message) {
        super(message);
    }

    public PrivateKeyReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
