package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.ICertificateGeneratorService;
import com.example.InfBezTim10.service.IUserService;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateGeneratorService extends JPAService<Certificate> implements ICertificateGeneratorService {
    private final ICertificateRepository certificateRepository;
    private final IUserService userService;
    private static String certDir = "certs";
    private Certificate issuer;
    private Date validTo;
    private User subject;
    private KeyPair currentRSA;
    private KeyUsage flags;
    private boolean isAuthority;

    @Autowired
    public CertificateGeneratorService(IUserService userService, ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
    }

    public Certificate IssueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo){
        try {
            Validate(issuerSN, subjectUsername, keyUsageFlags, validTo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        var cert = GenerateCertificate();
//
//        return ExportGeneratedCertificate(cert);
        return new Certificate();
    }

    private X509Certificate GenerateCertificate() throws NoSuchAlgorithmException {
        String subjectText = String.format("CN=%s", subject.getEmail());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        currentRSA = keyPairGenerator.generateKeyPair();

        CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        certGen.generate(4096);
        X500Name x500Name = new X500Name(subjectText);
        X509Certificate certificateRequest = certGen.getSelfCertificate(x500Name, validFrom.getTime(), validTo.getTime());

        var certificateRequest = new CertificateRequest(subjectText, currentRSA, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);

        certificateRequest.CertificateExtensions.Add(new X509BasicConstraintsExtension(isAuthority, false, 0, true));
        certificateRequest.CertificateExtensions.Add(new X509KeyUsageExtension(flags, false));

        var generatedCertificate = issuerCertificate == null
                ? certificateRequest.CreateSelfSigned(DateTime.Now, validTo)
                : certificateRequest.Create(issuerCertificate, DateTime.Now, validTo,
                Guid.NewGuid().ToByteArray());
        return generatedCertificate;
    }

    private void Validate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo) throws Exception, GeneralSecurityException {
        if (issuerSN.isEmpty()) {
            if (!(validTo.after(new Date()))) {
                throw new Exception("The date is not in the accepted range");
            }
        } else {
            issuer = certificateRepository.findBySerialNumber(issuerSN);
            //009CE3087D7227AE30

            X509Certificate issuerCertificate = readCertificateFromFile(String.format("%s/%s.crt", certDir, issuerSN));
            RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKeyFromBytes(String.format("%s/%s.key", certDir, issuerSN));
            issuerCertificate = copyWithPrivateKey(issuerCertificate, privateKey);

            if (!(validTo.after(new Date()) && validTo.before(issuerCertificate.getNotAfter()))) {
                throw new Exception("The date is not in the accepted range");
            }
        }
        this.validTo = validTo;
        subject = userService.findByEmail(subjectUsername);
        flags = new KeyUsage(parseFlags(keyUsageFlags));
    }

    private int parseFlags(String keyUsageFlags) throws Exception {
        if (keyUsageFlags == null || keyUsageFlags.isEmpty()) {
            throw new Exception("KeyUsageFlags are mandatory");
        }

        String[] flagArray = keyUsageFlags.split(",");
        int retVal = 0;

        Map<Integer, Integer> possibleElements = new HashMap<>();
        possibleElements.put(0, KeyUsage.digitalSignature);
        possibleElements.put(1, KeyUsage.nonRepudiation);
        possibleElements.put(2, KeyUsage.keyEncipherment);
        possibleElements.put(3, KeyUsage.dataEncipherment);
        possibleElements.put(4, KeyUsage.keyAgreement);
        possibleElements.put(5, KeyUsage.keyCertSign);
        possibleElements.put(6, KeyUsage.cRLSign);
        possibleElements.put(7, KeyUsage.encipherOnly);
        possibleElements.put(8, KeyUsage.decipherOnly);

        for (String flag : flagArray) {
            try {
                int index = Integer.parseInt(flag);
                int currentFlag = possibleElements.get(index);
                retVal |= currentFlag;

                if (currentFlag == KeyUsage.keyCertSign){
                    isAuthority = true;
                }
            } catch (NumberFormatException e) {
                throw new Exception("Unknown flag: " + flag);
            }
        }

        return retVal;
    }

    public static X509Certificate copyWithPrivateKey(X509Certificate certificate, PrivateKey privateKey) throws Exception {
        X500Name issuer = new X500Name(certificate.getIssuerX500Principal().getName());
        X500Name subject = new X500Name(certificate.getSubjectX500Principal().getName());
        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                certificate.getSerialNumber(),
                certificate.getNotBefore(),
                certificate.getNotAfter(),
                subject,
                publicKey
        );

        ContentSigner contentSigner = new JcaContentSignerBuilder(certificate.getSigAlgName()).build(privateKey);
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

    public static PrivateKey getPrivateKeyFromBytes(String path) throws GeneralSecurityException {
        try {
            byte[] keyBytes = Files.readAllBytes(new File(path).toPath());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private X509Certificate readCertificateFromFile(String path) {
        File certificateFile = new File(path);
        try (InputStream inStream = new FileInputStream(certificateFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected MongoRepository<Certificate, String> getEntityRepository() {
        return this.certificateRepository;
    }
}
