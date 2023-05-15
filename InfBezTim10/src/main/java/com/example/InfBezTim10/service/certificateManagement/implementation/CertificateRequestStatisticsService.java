package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.model.certificate.CertificateRequestStatus;
import com.example.InfBezTim10.repository.ICertificateRequestRepository;
import com.example.InfBezTim10.service.certificateManagement.ICertificateRequestStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificateRequestStatisticsService implements ICertificateRequestStatisticsService {
    private final ICertificateRequestRepository certificateRequestRepository;

    @Autowired
    public CertificateRequestStatisticsService(ICertificateRequestRepository certificateRequestRepository) {
        this.certificateRequestRepository = certificateRequestRepository;
    }

    @Override
    public long countAllCertificateRequests() {
        return certificateRequestRepository.count();
    }

    @Override
    public Long countAllCertificateRequestsByUser(String username) {
        return certificateRequestRepository.countByUsername(username) != null ? certificateRequestRepository.countByUsername(username) : 0L;
    }

    @Override
    public Long countCertificateRequestsByStatus(CertificateRequestStatus status) {
        return certificateRequestRepository.countByStatus(status) != null ? certificateRequestRepository.countByStatus(status) : 0L;
    }

    @Override
    public Long countAllCertificateRequestsByStatusAndUser(CertificateRequestStatus status, String username) {
        return certificateRequestRepository.countByStatusAndUsername(status, username) != null ? certificateRequestRepository.countByStatusAndUsername(status, username) : 0L;
    }
}
