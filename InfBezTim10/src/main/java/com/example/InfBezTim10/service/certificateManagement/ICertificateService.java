package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.service.base.IJPAService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICertificateService extends IJPAService<Certificate> {
    Certificate findBySerialNumber(String serialNumber);

    List<Certificate> findCertificatesSignedBy(String issuerSN);

    Page<Certificate> findCertificatesForUser(String userEmail, Pageable pageable);
    List<Certificate> findCertificatesForUser(String userEmail);

    void revokeCertificate(String serialNumber);

    long countAllCertificates();

    Long countCertificatesByStatus(CertificateStatus status);
}
