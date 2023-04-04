package com.example.InfBezTim10.service;

import com.example.InfBezTim10.exception.CertificateGenerationException;
import com.example.InfBezTim10.model.Certificate;

import java.util.Date;

public interface ICertificateGeneratorService {
    Certificate issueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo)
            throws CertificateGenerationException;
}
