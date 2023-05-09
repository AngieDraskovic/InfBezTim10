package com.example.InfBezTim10.exception.certificateRequest;

import com.example.InfBezTim10.exception.CustomException;

public class CertificateTypeHierarchyException extends CertificateRequestValidationException {
    public CertificateTypeHierarchyException(String message) {
        super(message);
    }

    public CertificateTypeHierarchyException(String message, Throwable cause) {
        super(message, cause);
    }
}