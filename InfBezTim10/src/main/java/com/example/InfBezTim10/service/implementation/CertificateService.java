package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.exception.CertificateGenerationException;
import com.example.InfBezTim10.model.*;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.ICertificateService;
import com.example.InfBezTim10.utils.CertificateConfigValidator;
import com.example.InfBezTim10.utils.CertificateFileUtils;
import com.example.InfBezTim10.utils.CertificateGenerator;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Service
public class CertificateService extends JPAService<Certificate> implements ICertificateService {

    private final ICertificateRepository certificateRepository;
    private final CertificateGenerator certificateGenerator;
    private final CertificateConfigValidator configValidator;
    private final IUserRepository userRepository;

    @Value("${cert.dir}")
    public static String certDir;

    @Autowired
    public CertificateService(ICertificateRepository certificateRepository, CertificateGenerator certificateGenerator,
                              CertificateConfigValidator configValidator, IUserRepository userRepository) {
        this.certificateRepository = certificateRepository;
        this.certificateGenerator = certificateGenerator;
        this.configValidator = configValidator;
        this.userRepository = userRepository;
    }

    public Certificate issueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo)
            throws CertificateGenerationException {

        try {
            CertificateConfig config = createCertificateConfig(issuerSN, subjectUsername, keyUsageFlags, validTo);
            configValidator.validate(config);
            X509Certificate cert = certificateGenerator.generateCertificate(config);

            return exportGeneratedCertificate(cert, config);
        } catch (CertificateException | NoSuchAlgorithmException | IOException | InvalidKeySpecException |
                 OperatorCreationException e) {
            throw new CertificateGenerationException("Failed to issue certificate", e);
        }
    }

    private CertificateConfig createCertificateConfig(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo) throws NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {
        CertificateConfig config = new CertificateConfig();

        Certificate issuer = issuerSN != null && !issuerSN.isEmpty()
                ? certificateRepository.findBySerialNumber(issuerSN)
                : null;
        X509Certificate issuerCertificate = null;
        if (issuer != null) {
            issuerCertificate = CertificateFileUtils.readCertificate(issuer.getSerialNumber());
        }
        User subject = userRepository.findByEmail(subjectUsername);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        config.setIssuer(issuer);
        config.setSubject(new X500Name("CN=" + subject.getEmail()));
        config.setSerialNumber(new BigInteger(64, new SecureRandom()));
        config.setValidFrom(new Date());
        config.setValidTo(validTo);
        config.setKeyPair(keyPair);
        config.setIssuerCertificate(issuerCertificate);
        config.setKeyUsage(parseFlags(keyUsageFlags, config));
        config.setSignatureAlgorithm("SHA256WithRSAEncryption");
        config.setSigningKey(issuerCertificate != null ? CertificateFileUtils.readPrivateKey(issuerSN) : keyPair.getPrivate());

        return config;
    }

    private Certificate exportGeneratedCertificate(X509Certificate cert, CertificateConfig config) throws IOException, CertificateEncodingException {
        Certificate certificateForDb = new Certificate();
        certificateForDb.setIssuer(config.getIssuer() != null ? config.getIssuer().getSerialNumber() : null);
        certificateForDb.setStatus(CertificateStatus.VALID);
        certificateForDb.setType(config.isAuthority()
                ? config.getIssuerCertificate() == null ? CertificateType.ROOT : CertificateType.INTERMEDIATE
                : CertificateType.END);
        certificateForDb.setSerialNumber(cert.getSerialNumber().toString(16));
        certificateForDb.setSignatureAlgorithm(cert.getSigAlgName());
        certificateForDb.setUserEmail(config.getSubject().toString().split("=")[1]);
        certificateForDb.setValidFrom(cert.getNotBefore());
        certificateForDb.setValidTo(cert.getNotAfter());

        certificateRepository.save(certificateForDb);

        CertificateFileUtils.writeCertificate(cert, certificateForDb.getSerialNumber());
        CertificateFileUtils.writePrivateKey(config.getKeyPair().getPrivate(), certificateForDb.getSerialNumber());

        return certificateForDb;
    }

    private KeyUsage parseFlags(String keyUsageFlags, CertificateConfig config) {
        if (keyUsageFlags == null || keyUsageFlags.isEmpty()) {
            throw new IllegalArgumentException("KeyUsageFlags are mandatory");
        }

        String[] flagArray = keyUsageFlags.split(",");
        int retVal = 0;
        config.setAuthority(false);

        for (String flag : flagArray) {
            try {
                int index = Integer.parseInt(flag);
                retVal |= 1 << index;

                if (index == 5) {
                    config.setAuthority(true);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown flag: " + flag, e);
            }
        }

        return new KeyUsage(retVal);
    }

    @Override
    protected MongoRepository<Certificate, String> getEntityRepository() {
        return certificateRepository;
    }
}
