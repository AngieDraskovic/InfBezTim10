package com.example.InfBezTim10.service.implementation;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.*;
import com.example.InfBezTim10.repository.ICertificateRequestRepository;
import com.example.InfBezTim10.service.ICertificateRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CertificateRequestService extends JPAService<CertificateRequest> implements ICertificateRequestService {

    private final ICertificateRequestRepository certificateRequestRepository;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository) {
        this.certificateRequestRepository = certificateRequestRepository;
    }

    @Override
    protected MongoRepository<CertificateRequest, String> getEntityRepository() {
        return this.certificateRequestRepository;
    }


    public CertificateRequest createCertificateRequest(CertificateRequestDTO certificateRequestDTO, String userRole) {
        // Validate the certificate type based on the user role
        if (!userRole.equals("ADMIN") && certificateRequestDTO.getCertificateType() == CertificateType.ROOT) {
            throw new IllegalArgumentException("Only admins can request root certificates.");
        }

        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setIssuerSN(certificateRequestDTO.getIssuerSN());
        certificateRequest.setSubjectUsername(certificateRequestDTO.getSubjectUsername());
        certificateRequest.setKeyUsageFlags(certificateRequestDTO.getKeyUsageFlags());
        certificateRequest.setValidTo(certificateRequestDTO.getValidTo());

        // Set status based on the rules mentioned in the task
        if (certificateRequest.getIssuerSN().equals(certificateRequest.getSubjectUsername()) || userRole.equals("ADMIN")) {
            certificateRequest.setStatus("APPROVED");
        } else {
            certificateRequest.setStatus("PENDING");
        }

        return certificateRequestRepository.save(certificateRequest);
    }



}
