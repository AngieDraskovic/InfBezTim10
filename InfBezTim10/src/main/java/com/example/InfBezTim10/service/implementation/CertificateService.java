package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateStatus;
import com.example.InfBezTim10.model.CertificateType;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.ICertificateService;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.security.auth.x500.X500Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.time.LocalDate;

@Service
public class CertificateService extends JPAService<Certificate> implements ICertificateService {

    private IUserRepository userRepository;
    private static String certDir = "certs";
    private Certificate issuer;
    private User subject;
    private KeyUsage keyUsage;
    private boolean isAuthority;
    private X509Certificate issuerCertificate;
    private Date validTo;
    private KeyPair currentKeyPair;

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
