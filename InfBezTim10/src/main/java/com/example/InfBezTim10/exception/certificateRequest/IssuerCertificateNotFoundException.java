package com.example.InfBezTim10.exception.certificateRequest;

import com.example.InfBezTim10.exception.CustomException;

public class IssuerCertificateNotFoundException extends CertificateRequestValidationException {
    public IssuerCertificateNotFoundException(String message) {
        super(message);
    }

    public IssuerCertificateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
