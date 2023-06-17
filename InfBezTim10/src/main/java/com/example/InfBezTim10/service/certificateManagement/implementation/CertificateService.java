package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.controller.UserController;
import com.example.InfBezTim10.exception.certificate.CertificateNotFoundException;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;

@Service
public class CertificateService extends MongoService<Certificate> implements ICertificateService {

    private final ICertificateRepository certificateRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public CertificateService(ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    public Certificate findBySerialNumber(String serialNumber) {
        return certificateRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> {
                    logger.error("Certificate with serial number {} not found", serialNumber);
                    return new CertificateNotFoundException("Certificate with serial number " + serialNumber + " not found.");
                });
    }

    @Override
    public List<Certificate> findCertificatesSignedBy(String issuerSN) {
        return certificateRepository.findSignedBy(issuerSN);
    }

    @Override
    public Page<Certificate> findCertificatesForUser(String userEmail, Pageable pageable) {
        return certificateRepository.findByUserEmail(userEmail, pageable);
    }

    @Override
    public List<Certificate> findCertificatesForUser(String userEmail) {
        return certificateRepository.findByUserEmail(userEmail);
    }

    @Override
    public void revokeCertificate(String serialNumber) {
        Certificate certificate = findBySerialNumber(serialNumber);
        certificate.setStatus(CertificateStatus.REVOKED);
        certificateRepository.save(certificate);

        List<Certificate> children = findCertificatesSignedBy(certificate.getSerialNumber());
        for (Certificate child : children) {
            revokeCertificate(child.getSerialNumber());
        }
    }

    @Override
    public long countAllCertificates() {
        return certificateRepository.count();
    }

    @Override
    public Long countCertificatesByStatus(CertificateStatus status) {
        Long count = certificateRepository.countByStatus(status);
        if (count == null) {
            count = 0L;
        }

        return count;
    }

    @Scheduled(cron = "0 0 0,12 * * ?")
    public void validateExpiredCertificates() {
        List<Certificate> certificatesToBeValidated = certificateRepository.findExpired(new Date());
        for (Certificate certificate : certificatesToBeValidated) {
//            TODO ADD EXPIRATION LOGIC
            certificate.setStatus(CertificateStatus.EXPIRED);
            certificateRepository.save(certificate);
        }
    }

    protected MongoRepository<Certificate, String> getEntityRepository() {
        return certificateRepository;
    }

}
