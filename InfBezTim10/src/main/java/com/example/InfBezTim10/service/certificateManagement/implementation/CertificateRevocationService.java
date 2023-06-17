package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.exception.certificate.CertificateNotFoundException;
import com.example.InfBezTim10.exception.certificate.CertificateRevocationException;
import com.example.InfBezTim10.exception.user.UserNotFoundException;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.model.certificate.CertificateType;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.certificateManagement.ICertificateRevocationService;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateRevocationService implements ICertificateRevocationService {

    private final ICertificateService certificateService;
    private final IUserService userService;

    @Autowired
    public CertificateRevocationService(ICertificateService certificateService, IUserService userService) {
        this.certificateService = certificateService;
        this.userService = userService;
    }

    @Override
    public void revoke(String certSN, String userEmail) throws CertificateRevocationException {
        try {
            User user = userService.findByEmail(userEmail);
            Certificate cert = certificateService.findBySerialNumber(certSN);
            if (!cert.getUserEmail().equals(user.getEmail()) && !user.getAuthority().getAuthorityName().equals("ROLE_ADMIN")) {
                throw new CertificateRevocationException("User does not have permission to revoke this certificate!");
            }

            revoke(cert);
        } catch (CertificateRevocationException | UserNotFoundException e) {
            throw new CertificateRevocationException(e.getMessage());
        }
    }

    @Override
    public void revoke(Certificate cert) throws CertificateRevocationException {
        try {
            if (cert.getStatus() == CertificateStatus.REVOKED) {
                throw new CertificateRevocationException("Certificate already revoked!");
            }

            if (cert.getType() == CertificateType.ROOT) {
                throw new CertificateRevocationException("Cannot revoke root certificate!");
            }

            List<Certificate> signedCertificates = certificateService.findCertificatesSignedBy(cert.serialNumber);
            for (Certificate signedCert : signedCertificates) {
                revoke(signedCert);
            }
        } catch (CertificateNotFoundException e) {
            throw new CertificateRevocationException("Certificate does not exist!");
        }
    }
}
