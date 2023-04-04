package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateStatus;
import com.example.InfBezTim10.model.CertificateType;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Service
public class CertificateService extends git JPAService<Certificate> implements ICertificateService {

    private static final String certDir = "certs";
    private final ICertificateRepository certificateRepository;
    private final IUserRepository userRepository;

    @Autowired
    public CertificateService(ICertificateRepository certificateRepository, IUserRepository userRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
    }

    protected MongoRepository<Certificate, String> getEntityRepository() {
        return this.certificateRepository;
    }




}
