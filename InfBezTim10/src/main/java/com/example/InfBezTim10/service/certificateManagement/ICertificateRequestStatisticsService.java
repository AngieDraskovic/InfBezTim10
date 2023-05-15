package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.model.certificate.CertificateRequestStatus;

public interface ICertificateRequestStatisticsService {
    long countAllCertificateRequests();

    Long countAllCertificateRequestsByUser(String username);

    Long countCertificateRequestsByStatus(CertificateRequestStatus status);

    Long countAllCertificateRequestsByStatusAndUser(CertificateRequestStatus status, String username);
}
