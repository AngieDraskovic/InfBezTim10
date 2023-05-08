package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.dto.certificate.CertificateRequestDTO;
import com.example.InfBezTim10.exception.certificate.CertificateGenerationException;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.mapper.CertificateRequestMapper;
import com.example.InfBezTim10.model.certificate.*;
import com.example.InfBezTim10.repository.ICertificateRequestRepository;
import com.example.InfBezTim10.service.certificateManagement.ICertificateGeneratorService;
import com.example.InfBezTim10.service.certificateManagement.ICertificateRequestService;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CertificateRequestService extends MongoService<CertificateRequest> implements ICertificateRequestService {

    private final ICertificateRequestRepository certificateRequestRepository;
    private final ICertificateService certificateService;
    private final ICertificateGeneratorService certificateGeneratorService;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository, ICertificateService certificateService, ICertificateGeneratorService certificateGeneratorService) {
        this.certificateRequestRepository = certificateRequestRepository;
        this.certificateService = certificateService;
        this.certificateGeneratorService = certificateGeneratorService;
    }


    @Override
    protected MongoRepository<CertificateRequest, String> getEntityRepository() {
        return this.certificateRequestRepository;
    }

    public List<CertificateRequest> getCertificateRequestsByUser(String username) {
        return certificateRequestRepository.findBySubjectUsername(username);
    }

    public CertificateRequest createCertificateRequest(CertificateRequestDTO certificateRequestDTO, String userRole, String currentUserEmail) throws CertificateGenerationException {

        CertificateRequest certificateRequest = CertificateRequestMapper.INSTANCE.certificateRequestDTOToCertificateRequest(certificateRequestDTO);

        if(certificateRequest.getIssuerSN() == null)
        {
            throw new IllegalArgumentException("Root certificate can not be issued.");
        }

        // Fetch the certificate by its serial number
        Certificate issuerCertificate = certificateService.findBySerialNumber(certificateRequest.getIssuerSN());

        if (issuerCertificate == null) {
            throw new IllegalArgumentException("Issuer certificate not found.");
        }

        if (issuerCertificate.getType() == CertificateType.END){
            throw new IllegalArgumentException("Can not issue certificate based on end certificate.");
        }

        if (issuerCertificate.getStatus() != CertificateStatus.VALID){
            throw new IllegalArgumentException("Can not issue certificate based on invalid certificate.");
        }

        Date currentDate = new Date();

        if (currentDate.before(issuerCertificate.getValidFrom()) || currentDate.after(issuerCertificate.getValidTo())) {
            throw new IllegalArgumentException("Can not issue certificate based on invalid certificate.");
        }

        String certificateEmail = issuerCertificate.getUserEmail();

        if (certificateEmail.equals(currentUserEmail) || userRole.equals("ROLE_ADMIN")) {
            certificateGeneratorService.issueCertificate(certificateRequest.getIssuerSN(), certificateRequest.getSubjectUsername(), certificateRequest.getKeyUsageFlags(), certificateRequest.getValidTo());
            certificateRequest.setStatus(CertificateRequestStatus.APPROVED);
        } else {
            certificateRequest.setStatus(CertificateRequestStatus.PENDING);
        }
        return certificateRequestRepository.save(certificateRequest);
    }


    public Certificate approveCertificateRequest(String requestId, String username) throws CertificateGenerationException {
        CertificateRequest request = getCertificateRequestByIdAndUsername(requestId, username);

        Certificate generatedCertificate = certificateGeneratorService.issueCertificate(
                request.getIssuerSN(), request.getSubjectUsername(),
                request.getKeyUsageFlags(), request.getValidTo()
        );

        request.setStatus(CertificateRequestStatus.APPROVED);
        certificateRequestRepository.save(request);

        return generatedCertificate;
    }

    public void rejectCertificateRequest(String requestId, String username, String rejectionReason) {
        CertificateRequest request = getCertificateRequestByIdAndUsername(requestId, username);

        request.setStatus(CertificateRequestStatus.REJECTED);
        request.setReason(rejectionReason);
        certificateRequestRepository.save(request);
    }

    private CertificateRequest getCertificateRequestByIdAndUsername(String requestId, String username) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Certificate request not found"));

        Certificate certificate = certificateService.findBySerialNumber(request.getIssuerSN());

        if (!certificate.getUserEmail().equals(username)) {
            throw new IllegalArgumentException("User is not authorized to approve or reject this certificate request");
        }

        return request;
    }



}

