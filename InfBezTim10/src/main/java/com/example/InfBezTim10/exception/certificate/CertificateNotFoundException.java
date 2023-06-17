package com.example.InfBezTim10.exception.certificate;

import com.example.InfBezTim10.exception.NotFoundException;

public class CertificateNotFoundException extends NotFoundException {
    public CertificateNotFoundException(String message) {
        super(message);
    }
}
