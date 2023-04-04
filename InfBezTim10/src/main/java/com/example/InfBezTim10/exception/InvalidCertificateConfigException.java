package com.example.InfBezTim10.exception;

public class InvalidCertificateConfigException extends IllegalArgumentException {
    public InvalidCertificateConfigException(String message) {
        super(message);
    }

    public InvalidCertificateConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
