package com.example.InfBezTim10.exception.certificateRequest;

import com.example.InfBezTim10.exception.CustomException;

public class EndCertificateUsageException extends CertificateRequestValidationException {
    public EndCertificateUsageException(String message) {
        super(message);
    }

    public EndCertificateUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
