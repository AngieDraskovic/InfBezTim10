package com.example.InfBezTim10.service.implementation;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.*;
import com.example.InfBezTim10.repository.ICertificateRequestRepository;
import com.example.InfBezTim10.service.ICertificateRequestService;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CertificateRequestService extends MongoService<CertificateRequest>  implements ICertificateRequestService {

    private final ICertificateRequestRepository certificateRequestRepository;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository) {
        this.certificateRequestRepository = certificateRequestRepository;
    }


    @Override
    protected MongoRepository<CertificateRequest, String> getEntityRepository() {
        return this.certificateRequestRepository;
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

        return  CertificateType.END;
    }



    public CertificateRequest createCertificateRequest(CertificateRequestDTO certificateRequestDTO, String userRole) {
        // Validate the certificate type based on the user role
        if (!userRole.equals("ADMIN") && getCertificateType(certificateRequestDTO.getKeyUsageFlags()) == CertificateType.ROOT) {
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

