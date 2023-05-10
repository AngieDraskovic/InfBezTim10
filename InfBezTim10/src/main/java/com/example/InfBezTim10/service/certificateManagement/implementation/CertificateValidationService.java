package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.exception.certificate.CertificateValidationException;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.model.certificate.CertificateType;
import com.example.InfBezTim10.service.certificateManagement.ICertificateValidationService;
import com.example.InfBezTim10.utils.CertificateFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

@Service
public class CertificateValidationService implements ICertificateValidationService {

    private static final String rootSN = "5b2b4cc6fa7119b9";

    private final CertificateFileUtils certificateFileUtils;
    private final CertificateService certificateService;

    @Autowired
    public CertificateValidationService(CertificateFileUtils certificateFileUtils, CertificateService certificateService) {
        this.certificateFileUtils = certificateFileUtils;
        this.certificateService = certificateService;
    }

    @Override
    public void validate(MultipartFile file) throws CertificateValidationException {
        try {
            X509Certificate cert = certificateFileUtils.convertMultipartFileToX509Certificate(file);
            validate(cert.getSerialNumber().toString(16));
        } catch (IOException | CertificateException e) {
            throw new CertificateValidationException("Certificate copy is not valid!", e);
        }
    }



    private void validateCertificateChain(Certificate currCert, X509Certificate currX509Cert) throws CertificateValidationException, CertificateException, IOException {
        if (currCert == null) {
            throw new CertificateValidationException("Root certificate is not found!");
        }

        validateStatus(currCert);

        if (currCert.getSerialNumber().equals(rootSN)) {
            return;
        }

        if (currCert.type == CertificateType.ROOT) {
            throw new CertificateValidationException("Root certificate is not trusted!");
        }

        Certificate issuerCert = certificateService.findBySerialNumber(currCert.getIssuer());
        X509Certificate issuerX509Cert = certificateFileUtils.readCertificate(issuerCert.serialNumber);
        validateCertificateSignature(currX509Cert, issuerX509Cert);
        validateCertificateChain(issuerCert, issuerX509Cert);
    }
    
    @Override
    public void validate(String certSN) throws CertificateValidationException {
        try {
            Certificate cert = certificateService.findBySerialNumber(certSN);
            X509Certificate x509Cert = certificateFileUtils.readCertificate(certSN);

            validateCertificateChain(cert, x509Cert);
            validateExpiration(cert);
            validateStatus(cert);
        } catch (IOException | CertificateException e) {
            throw new CertificateValidationException(e.getMessage(), e);
        }
    }


    private void validateExpiration(Certificate cert) throws CertificateValidationException {
        Date currentTime = Calendar.getInstance().getTime();

        if (cert.getValidFrom().after(currentTime)) {
            throw new CertificateValidationException("Certificate is not yet valid!");
        }

        if (cert.getValidTo().before(currentTime)) {
            throw new CertificateValidationException("Certificate is no longer valid!");
        }
    }

    private void validateStatus(Certificate cert) throws CertificateValidationException {
        if (cert.getStatus() != CertificateStatus.VALID) {
            throw new CertificateValidationException("Certificate is not valid!");
        }
    }

    private void validateCertificateSignature(X509Certificate cert, X509Certificate issuerCert) throws CertificateValidationException {
        try {
            Signature signature = Signature.getInstance(cert.getSigAlgName());
            signature.initVerify(issuerCert.getPublicKey());
            signature.update(cert.getTBSCertificate());
            if (!signature.verify(cert.getSignature())) {
                throw new CertificateValidationException("Certificate signature is not valid!");
            }

        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | CertificateException e) {
            throw new CertificateValidationException("Error occurred while validating certificate signature", e);
        }
    }
}
