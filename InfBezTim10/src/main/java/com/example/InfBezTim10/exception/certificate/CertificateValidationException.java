package com.example.InfBezTim10.exception.certificate;

public class CertificateValidationException extends Exception {

    public CertificateValidationException(String message) {
        super(message);
    }

    public CertificateValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
