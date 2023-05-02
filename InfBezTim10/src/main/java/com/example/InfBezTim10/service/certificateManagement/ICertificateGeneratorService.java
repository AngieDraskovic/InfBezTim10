package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.exception.certificate.CertificateGenerationException;
import com.example.InfBezTim10.model.certificate.Certificate;

import java.util.Date;

public interface ICertificateGeneratorService {
    Certificate issueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo)
            throws CertificateGenerationException;
}
