package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateStatus;
import com.example.InfBezTim10.model.CertificateType;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CertificateService extends JPAService<Certificate> implements ICertificateService {

    private final ICertificateRepository certificateRepository;

    @Autowired
    public CertificateService(ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    protected MongoRepository<Certificate, String> getEntityRepository() {
        return this.certificateRepository;
    }

//    public Certificate generateCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, DateTime validTo) throws Exception {
//
//    }



}
