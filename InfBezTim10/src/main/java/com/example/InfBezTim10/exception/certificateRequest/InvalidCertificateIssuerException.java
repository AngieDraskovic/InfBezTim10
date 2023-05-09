package com.example.InfBezTim10.exception.certificateRequest;

import com.example.InfBezTim10.exception.CustomException;

public class InvalidCertificateIssuerException extends CertificateRequestValidationException {
    public InvalidCertificateIssuerException(String message) {
        super(message);
    }

    public InvalidCertificateIssuerException(String message, Throwable cause) {
        super(message, cause);
    }
}
