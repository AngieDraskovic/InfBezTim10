package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateStatus;
import com.example.InfBezTim10.model.CertificateType;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.ICertificateService;
import com.example.InfBezTim10.utils.X509CertificateGeneratorUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

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

    public Certificate issueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
            SignatureException, IOException, InvalidKeySpecException, OperatorCreationException {
        validate(issuerSN, subjectUsername, keyUsageFlags, validTo);
        X509Certificate cert = generateCertificate();

        return exportGeneratedCertificate(cert);
    }


    private Certificate exportGeneratedCertificate(X509Certificate cert) throws IOException, CertificateEncodingException {
        Certificate certificateForDb = new Certificate();
        certificateForDb.setIssuer(issuer != null ? issuer.getSerialNumber() : null);
        certificateForDb.setStatus(CertificateStatus.VALID);
        certificateForDb.setType(isAuthority
                ? issuerCertificate == null ? CertificateType.ROOT : CertificateType.INTERMEDIATE
                : CertificateType.END);
        certificateForDb.setSerialNumber(cert.getSerialNumber().toString(16));
        certificateForDb.setSignatureAlgorithm(cert.getSigAlgName());
        certificateForDb.setUserEmail(subject.getName());
        certificateForDb.setValidFrom(cert.getNotBefore());
        certificateForDb.setValidTo(cert.getNotAfter());

        certificateRepository.save(certificateForDb);

        Files.write(Paths.get(certDir, certificateForDb.getSerialNumber() + ".crt"),
                cert.getEncoded());
        Files.write(Paths.get(certDir, certificateForDb.getSerialNumber() + ".key"),
                currentKeyPair.getPrivate().getEncoded());

        return certificateForDb;
    }


    private void validate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo)
            throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (issuerSN != null && !issuerSN.isEmpty()) {
            issuer = certificateRepository.findBySerialNumber(issuerSN);
            issuerCertificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
                    Files.newInputStream(Paths.get(certDir, issuerSN + ".crt")));
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(certDir, issuerSN + ".key"))));
            issuerCertificate = X509CertificateGeneratorUtils.copyWithPrivateKey(issuerCertificate, privateKey);
        }

        if (!(validTo.after(new Date()) && (issuerSN == null || validTo.before(issuerCertificate.getNotAfter())))) {
            throw new IllegalArgumentException("The date is not in the accepted range");
        }

        this.validTo = validTo;

        subject = userRepository.findByEmail(subjectUsername);
        keyUsage = parseFlags(keyUsageFlags);
    }



    private X509Certificate generateCertificate()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, OperatorCreationException, IOException {
        X500Name subjectText = new X500Name("CN=" + subject.getName());
        currentKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        X509v3CertificateBuilder certificateBuilder;
        if (issuerCertificate == null) {
            certificateBuilder = new JcaX509v3CertificateBuilder(
                    subjectText,
                    new BigInteger(64, new SecureRandom()),
                    new Date(),
                    validTo,
                    subjectText,
                    currentKeyPair.getPublic());
        } else {
            certificateBuilder = new JcaX509v3CertificateBuilder(
                    new X500Name(issuerCertificate.getSubjectX500Principal().getName()),
                    new BigInteger(64, new SecureRandom()),
                    new Date(),
                    validTo,
                    subjectText,
                    currentKeyPair.getPublic());
        }

        addExtensions(certificateBuilder);

        ContentSigner contentSigner;
        if (issuerCertificate == null) {
            contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(currentKeyPair.getPrivate());
        } else {
            AsymmetricKeyParameter privateKeyParameter = PrivateKeyFactory.createKey(issuerCertificate.getPublicKey().getEncoded());
            contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build((PrivateKey) privateKeyParameter);
        }

        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certificateHolder);
    }


    private void addExtensions(X509v3CertificateBuilder certificateBuilder) throws CertIOException, NoSuchAlgorithmException {
        certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isAuthority));
        certificateBuilder.addExtension(Extension.keyUsage, true, keyUsage);
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(currentKeyPair.getPublic().getEncoded());
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, new JcaX509ExtensionUtils().createSubjectKeyIdentifier(publicKeyInfo));

        if (issuerCertificate != null) {
            SubjectPublicKeyInfo issuerPublicKeyInfo = SubjectPublicKeyInfo.getInstance(issuerCertificate.getPublicKey().getEncoded());
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(issuerPublicKeyInfo));
        }

    }

        private KeyUsage parseFlags(String keyUsageFlags) {
        if (keyUsageFlags == null || keyUsageFlags.isEmpty()) {
            throw new IllegalArgumentException("KeyUsageFlags are mandatory");
        }

        String[] flagArray = keyUsageFlags.split(",");
        int retVal = 0;

        for (String flag : flagArray) {
            try {
                int index = Integer.parseInt(flag);
                retVal |= 1 << index;

                if (index == 5) {
                    isAuthority = true;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown flag: " + flag, e);
            }
        }

        return new KeyUsage(retVal);
    }



    @Override
    protected MongoRepository<Certificate, String> getEntityRepository() {
        return this.certificateRepository;
    }

//    public Certificate generateCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, DateTime validTo) throws Exception {
//
//    }



}
