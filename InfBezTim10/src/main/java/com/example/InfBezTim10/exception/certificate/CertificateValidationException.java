package com.example.InfBezTim10.exception.certificate;

import com.example.InfBezTim10.exception.CustomException;

public class CertificateValidationException extends CustomException {

    public CertificateValidationException(String message) {
        super(message);
    }

    public CertificateValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
