package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.exception.certificate.CertificateNotFoundException;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;

@Service
public class CertificateService extends MongoService<Certificate> implements ICertificateService {

    private final ICertificateRepository certificateRepository;

    @Autowired
    public CertificateService(ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    public Certificate findBySerialNumber(String serialNumber) {
        return certificateRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new CertificateNotFoundException("Certificate with serial number " + serialNumber + " not found."));
    }

    @Override
    public List<Certificate> findCertificatesSignedBy(String issuerSN) {
        return certificateRepository.findCertificatesSignedBy(issuerSN);
    }

    @Scheduled(cron = "0 0 0,12 * * ?")
    public void validatePendingCertificates() {
        List<Certificate> certificatesToBeValidated = certificateRepository.findCertificatesToBeValidated(new Date());
        for (Certificate certificate : certificatesToBeValidated) {
            certificate.setStatus(CertificateStatus.VALID);
            certificateRepository.save(certificate);
        }
    }

    protected MongoRepository<Certificate, String> getEntityRepository() {
        return certificateRepository;
    }

}
