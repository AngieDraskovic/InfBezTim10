package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.exception.certificate.CertificateGenerationException;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.exception.certificateRequest.*;
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
import java.util.stream.Collectors;

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

    @Override
    public List<CertificateRequest> getOutgoingRequestsForUser(String username) {
        return certificateRequestRepository.findBySubjectUsername(username);
    }

    @Override
    public List<CertificateRequest> getPendingIncomingRequestsForUser(String userEmail) {
        List<String> issuerSerialNumbers = certificateService.findCertificatesForUser(userEmail)
                .stream()
                .map(Certificate::getSerialNumber)
                .collect(Collectors.toList());
        return certificateRequestRepository.findPendingIncomingRequestsByIssuerSerialNumbers(issuerSerialNumbers);
    }

    public CertificateRequest createCertificateRequest(CertificateRequest certificateRequest, String userRole, String currentUserEmail) {

        certificateRequest.setKeyUsageFlags(getKeyUsageFlags(certificateRequest.getCertificateType()));

        if(certificateRequest.getCertificateType() == CertificateType.ROOT && !userRole.equals("ROLE_ADMIN"))
        {
            throw new IssuerCertificateNotFoundException("User can not get root certificate.");
        }
        if(certificateRequest.getCertificateType() == CertificateType.ROOT && userRole.equals("ROLE_ADMIN"))
        {
            certificateGeneratorService.issueCertificate(certificateRequest.getIssuerSN(), certificateRequest.getSubjectUsername(), certificateRequest.getKeyUsageFlags(), certificateRequest.getValidTo());
            return certificateRequestRepository.save(certificateRequest);
        }

        Certificate issuerCertificate = certificateService.findBySerialNumber(certificateRequest.getIssuerSN());
        validateCertificateRequest(certificateRequest, issuerCertificate);
        certificateRequest.setKeyUsageFlags(getKeyUsageFlags(certificateRequest.getCertificateType()));

        String issuerEmail = issuerCertificate.getUserEmail();
        if (issuerEmail.equals(currentUserEmail) || userRole.equals("ROLE_ADMIN")) {
            certificateGeneratorService.issueCertificate(certificateRequest.getIssuerSN(), certificateRequest.getSubjectUsername(), certificateRequest.getKeyUsageFlags(), certificateRequest.getValidTo());
            certificateRequest.setStatus(CertificateRequestStatus.APPROVED);
        } else {
            certificateRequest.setStatus(CertificateRequestStatus.PENDING);
        }

        return certificateRequestRepository.save(certificateRequest);
    }

    private String getKeyUsageFlags(CertificateType certificateType) {
        return switch (certificateType) {
            case ROOT -> "1,3,5,7";
            case INTERMEDIATE -> "1,3,5,7,8";
            case END -> "1,3,5,7,9";
        };
    }

    private void validateCertificateRequest(CertificateRequest certificateRequest, Certificate issuerCertificate) {
        if (issuerCertificate == null) {
            throw new IssuerCertificateNotFoundException("Issuer certificate not found.");
        }

        if (issuerCertificate.getType() == CertificateType.END) {
            throw new EndCertificateUsageException("Cannot issue certificate based on end certificate.");
        }

        if (issuerCertificate.getStatus() != CertificateStatus.VALID) {
            throw new InvalidCertificateIssuerException("Cannot issue certificate based on invalid certificate.");
        }

        Date currentDate = new Date();
        if (certificateRequest.getValidTo().before(currentDate) || certificateRequest.getValidTo().after(issuerCertificate.getValidTo())) {
            throw new IssuerCertificateEndTimeException("Issuer certificate end time should be after the new certificate end time.");
        }

        if (issuerCertificate.getType().ordinal() > certificateRequest.getCertificateType().ordinal()) {
            throw new CertificateTypeHierarchyException("Issuer certificate type should be higher or equal to the type of the certificate being issued.");
        }
    }

    public Certificate approveCertificateRequest(String requestId, String username) throws CertificateGenerationException {
        CertificateRequest request = getCertificateRequestByIdAndUsername(requestId, username);

        Certificate generatedCertificate = certificateGeneratorService.issueCertificate(
                request.getIssuerSN(),
                request.getSubjectUsername(),
                request.getKeyUsageFlags(),
                request.getValidTo()
        );

        request.setStatus(CertificateRequestStatus.APPROVED);
        certificateRequestRepository.save(request);

        return generatedCertificate;
    }

    public void rejectCertificateRequest(String requestId, String username, String rejectionReason) {
        CertificateRequest request = getCertificateRequestByIdAndUsername(requestId, username);

        request.setStatus(CertificateRequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
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

