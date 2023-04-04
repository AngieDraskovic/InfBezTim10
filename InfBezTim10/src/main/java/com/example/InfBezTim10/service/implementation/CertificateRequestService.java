package com.example.InfBezTim10.service.implementation;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.exception.CertificateGenerationException;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.mapper.CertificateRequestMapper;
import com.example.InfBezTim10.model.*;
import com.example.InfBezTim10.repository.ICertificateRequestRepository;
import com.example.InfBezTim10.service.ICertificateGeneratorService;
import com.example.InfBezTim10.service.ICertificateRequestService;
import com.example.InfBezTim10.service.ICertificateService;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CertificateRequestService extends MongoService<CertificateRequest>  implements ICertificateRequestService {

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

    private static KeyUsage parseFlags(String keyUsageFlags) {
        if (keyUsageFlags == null || keyUsageFlags.isEmpty()) {
            throw new IllegalArgumentException("KeyUsageFlags are mandatory");
        }
        String[] flagArray = keyUsageFlags.split(",");
        int retVal = 0;

        for (String flag : flagArray) {
            try {
                int index = Integer.parseInt(flag);
                retVal |= 1 << index;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown flag: " + flag, e);
            }
        }
        return new KeyUsage(retVal);
    }


    private static CertificateType getCertificateType(String keyUsageString) {

        KeyUsage keyUsage = parseFlags(keyUsageString);

        int usageFlags = keyUsage.getBytes()[0]; // Get the byte array of the key usage flags and use the first byte

        if ((usageFlags & KeyUsage.keyCertSign) != 0) {
            return CertificateType.ROOT;
        } else if ((usageFlags & KeyUsage.cRLSign) != 0) {
            return CertificateType.INTERMEDIATE;
        }

        return CertificateType.END;
    }



    public CertificateRequest createCertificateRequest(CertificateRequestDTO certificateRequestDTO, String userRole, String currentUserEmail) throws CertificateGenerationException {
        // Validate the certificate type based on the user role
        if (!userRole.equals("ROLE_ADMIN") && getCertificateType(certificateRequestDTO.getKeyUsageFlags()) == CertificateType.ROOT) {
            throw new IllegalArgumentException("Only admins can request root certificates.");
        }

        CertificateRequest certificateRequest = CertificateRequestMapper.INSTANCE.certificateRequestDTOToCertificateRequest(certificateRequestDTO);

        String cerificateEmail = "";

        if (certificateRequestDTO.getIssuerSN() != null)
        {
            // Fetch the certificate by its serial number
            Certificate issuerCertificate = certificateService.findBySerialNumber(certificateRequestDTO.getIssuerSN());
            cerificateEmail = issuerCertificate.getUserEmail();
        }

        // Set status based on the rules mentioned in the task
        if (cerificateEmail.equals(currentUserEmail) || userRole.equals("ROLE_ADMIN")) {

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

        if (!request.getSubjectUsername().equals(username)) {
            throw new IllegalArgumentException("User is not authorized to approve or reject this certificate request");
        }

        return request;
    }



}

