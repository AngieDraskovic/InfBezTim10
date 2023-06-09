package com.example.InfBezTim10.service.certificateManagement.implementation;

import com.example.InfBezTim10.exception.certificate.CertificateGenerationException;
import com.example.InfBezTim10.exception.certificate.CertificateNotFoundException;
import com.example.InfBezTim10.exception.certificate.InvalidCertificateConfigException;
import com.example.InfBezTim10.exception.user.UserNotFoundException;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateConfig;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.model.certificate.CertificateType;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.certificateManagement.ICertificateGeneratorService;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import com.example.InfBezTim10.utils.CertificateConfigValidator;
import com.example.InfBezTim10.utils.CertificateFileUtils;
import com.example.InfBezTim10.utils.CertificateGenerator;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CertificateGeneratorService implements ICertificateGeneratorService {

    private final ICertificateService certificateService;
    private final IUserService userService;
    private final CertificateFileUtils certificateFileUtils;
    private final CertificateGenerator certificateGenerator;
    private final CertificateConfigValidator configValidator;

    @Autowired
    public CertificateGeneratorService(ICertificateService certificateService, IUserService userService, CertificateFileUtils certificateFileUtils, CertificateGenerator certificateGenerator, CertificateConfigValidator configValidator) {
        this.certificateService = certificateService;
        this.userService = userService;
        this.certificateFileUtils = certificateFileUtils;
        this.certificateGenerator = certificateGenerator;
        this.configValidator = configValidator;
    }

    @Override
    public Certificate issueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo)
            throws CertificateGenerationException {

        try {
            Certificate issuer = null;
            if (issuerSN != null && !issuerSN.isEmpty()) {
                issuer = certificateService.findBySerialNumber(issuerSN);
            }

            User subject = userService.findByEmail(subjectUsername);

            CertificateConfig config = createCertificateConfig(issuer, subject, keyUsageFlags, validTo);
            configValidator.validate(config);
            X509Certificate cert = certificateGenerator.generateCertificate(config);

            return exportGeneratedCertificate(cert, config);
        } catch (CertificateException | NoSuchAlgorithmException | IOException | InvalidKeySpecException |
                 OperatorCreationException | InvalidCertificateConfigException | CertificateNotFoundException
                 | UserNotFoundException e) {
            throw new CertificateGenerationException("Failed to issue certificate", e);
        }
    }

    private CertificateConfig createCertificateConfig(Certificate issuer, User subject, String keyUsageFlags, Date validTo) throws NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {
        CertificateConfig config = new CertificateConfig();

        X509Certificate issuerCertificate = null;
        if (issuer != null) {
            issuerCertificate = certificateFileUtils.readCertificate(issuer.getSerialNumber());
        }
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
        config.setSigningKey(issuerCertificate != null ? certificateFileUtils.readPrivateKey(issuer.getSerialNumber()) : keyPair.getPrivate());

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

        certificateService.save(certificateForDb);

        certificateFileUtils.writeCertificate(cert, certificateForDb.getSerialNumber());
        certificateFileUtils.writePrivateKey(config.getKeyPair().getPrivate(), certificateForDb.getSerialNumber());

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
}
