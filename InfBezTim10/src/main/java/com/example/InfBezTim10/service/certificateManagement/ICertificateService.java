package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.service.base.IJPAService;

import java.util.List;

public interface ICertificateService extends IJPAService<Certificate> {
    Certificate findBySerialNumber(String serialNumber);

    List<Certificate> findCertificatesSignedBy(String issuerSN);
}
