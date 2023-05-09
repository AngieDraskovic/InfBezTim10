package com.example.InfBezTim10.exception.certificateRequest;

import com.example.InfBezTim10.exception.CustomException;

public class CertificateRequestValidationException extends CustomException {
    public CertificateRequestValidationException(String message) {
        super(message);
    }

    public CertificateRequestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
