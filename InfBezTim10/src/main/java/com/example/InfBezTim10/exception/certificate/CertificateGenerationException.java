package com.example.InfBezTim10.exception.certificate;

import com.example.InfBezTim10.exception.CustomException;

public class CertificateGenerationException extends CustomException {
    public CertificateGenerationException(String message) {
        super(message);
    }

    public CertificateGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
