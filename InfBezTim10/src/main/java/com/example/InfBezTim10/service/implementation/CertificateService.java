package com.example.InfBezTim10.service.implementation;


import com.example.InfBezTim10.exception.CertificateNotFoundException;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateStatus;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class CertificateService extends MongoService<Certificate> implements ICertificateService {

    private final ICertificateRepository certificateRepository;

    @Autowired
    public CertificateService(ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    public Certificate findBySerialNumber(String serialNumber) {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        if (certificate == null) {
            throw new CertificateNotFoundException("Certificate with serial number " + serialNumber + " not found.");
        }

        return certificate;
    }

    @Override
    public boolean validate(String serialNumber) {
        Certificate certificate = findBySerialNumber(serialNumber);
        if (certificate.getStatus() != CertificateStatus.VALID) {
            return false;
        }
        if (certificate.getValidTo().before(Calendar.getInstance().getTime())) {
            certificate.setStatus(CertificateStatus.INVALID);
            save(certificate);
            return false;
        }

        return true;

    }

    protected MongoRepository<Certificate, String> getEntityRepository() {
        return certificateRepository;
    }

}
