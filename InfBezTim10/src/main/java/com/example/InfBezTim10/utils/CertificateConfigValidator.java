package com.example.InfBezTim10.utils;

import com.example.InfBezTim10.exception.InvalidCertificateConfigException;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateConfig;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.IUserService;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Component
public class CertificateConfigValidator {

    private final ICertificateRepository certificateRepository;
    private final IUserService userService;

    @Autowired
    public CertificateConfigValidator(ICertificateRepository certificateRepository, IUserService userService) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
    }

    public void validate(CertificateConfig config) throws InvalidCertificateConfigException {
//        validateIssuer(config.getIssuer());
        validateIssuerCertificate(config.getIssuerCertificate());
        validateSubject(config.getSubject());
        validateKeyPair(config.getKeyPair());
        validateValidFrom(config.getValidFrom());
        validateValidTo(config.getValidFrom(), config.getValidTo(), config.getIssuerCertificate());
        validateKeyUsage(config.getKeyUsage());
        validateSignatureAlgorithm(config.getSignatureAlgorithm());
        validateSigningKey(config.getSigningKey());
    }

//    private void validateIssuer(Certificate issuer) {
//        if (issuer != null) {
//            Certificate foundIssuer = certificateRepository.findBySerialNumber(issuer);
//            if (foundIssuer == null) {
//                throw new InvalidCertificateConfigException("The issuer is not found in the database");
//            }
//        }
//    }
    private void validateIssuerCertificate(X509Certificate issuerCertificate) {
        if (issuerCertificate != null) {
            try {
                issuerCertificate.checkValidity(new Date());
            } catch (CertificateExpiredException e) {
                throw new InvalidCertificateConfigException("Issuer certificate has expired", e);
            } catch (CertificateNotYetValidException e) {
                throw new InvalidCertificateConfigException("Issuer certificate is not yet valid", e);
            }
        }
    }

    private void validateSubject(X500Name subject) {
        if (subject == null) {
            throw new InvalidCertificateConfigException("Subject is mandatory");
        }

        User foundUser = userService.findByEmail(subject.toString().split("=")[1]);
        if (foundUser == null) {
            throw new InvalidCertificateConfigException("The subject user is not found in the database");
        }
    }

    private void validateKeyPair(KeyPair keyPair) {
        if (keyPair == null) {
            throw new IllegalArgumentException("KeyPair is mandatory");
        }
    }

    private void validateValidFrom(Date validFrom) {
        if (validFrom == null) {
            throw new IllegalArgumentException("ValidFrom is mandatory");
        }
    }

    private void validateValidTo(Date validFrom, Date validTo, X509Certificate issuerCertificate) {
        if (validTo == null) {
            throw new InvalidCertificateConfigException("ValidTo is mandatory");
        }

        if (validTo.before(validFrom)) {
            throw new InvalidCertificateConfigException("ValidTo must be after ValidFrom");
        }

        if (issuerCertificate != null && validTo.after(issuerCertificate.getNotAfter())) {
            throw new InvalidCertificateConfigException("ValidTo must be before issuer's NotAfter");
        }
    }

    private void validateKeyUsage(KeyUsage keyUsage) {
        if (keyUsage == null) {
            throw new InvalidCertificateConfigException("KeyUsage is mandatory");
        }
    }

    private void validateSignatureAlgorithm(String signatureAlgorithm) {
        if (signatureAlgorithm == null) {
            throw new InvalidCertificateConfigException("SignatureAlgorithm is mandatory");
        }
    }

    private void validateSigningKey(PrivateKey signingKey) {
        if (signingKey == null) {
            throw new InvalidCertificateConfigException("SigningKey is mandatory");
        }
    }
}
