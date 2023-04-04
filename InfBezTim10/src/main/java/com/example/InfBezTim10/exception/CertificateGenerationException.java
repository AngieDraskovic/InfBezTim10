package com.example.InfBezTim10.exception;

public class CertificateGenerationException extends Exception {
    public CertificateGenerationException(String message) {
        super(message);
    }

    public CertificateGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
