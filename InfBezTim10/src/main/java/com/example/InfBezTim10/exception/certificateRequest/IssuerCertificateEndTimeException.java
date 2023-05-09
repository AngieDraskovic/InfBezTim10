package com.example.InfBezTim10.exception.certificateRequest;

import com.example.InfBezTim10.exception.CustomException;

public class IssuerCertificateEndTimeException extends CertificateRequestValidationException {
    public IssuerCertificateEndTimeException(String message) {
        super(message);
    }

    public IssuerCertificateEndTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
