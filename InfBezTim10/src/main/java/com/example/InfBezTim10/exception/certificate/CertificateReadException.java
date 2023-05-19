package com.example.InfBezTim10.exception.certificate;

import com.example.InfBezTim10.exception.CustomException;

public class CertificateReadException extends CustomException {
    public CertificateReadException(String message) {
        super(message);
    }

    public CertificateReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
