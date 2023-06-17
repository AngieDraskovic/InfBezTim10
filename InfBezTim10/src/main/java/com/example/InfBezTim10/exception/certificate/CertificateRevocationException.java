package com.example.InfBezTim10.exception.certificate;

public class CertificateRevocationException extends Exception {
    public CertificateRevocationException(String message) {
        super(message);
    }

    public CertificateRevocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
