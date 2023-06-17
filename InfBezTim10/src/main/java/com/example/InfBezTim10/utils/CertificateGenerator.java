package com.example.InfBezTim10.utils;

import com.example.InfBezTim10.model.certificate.CertificateConfig;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Component
public class CertificateGenerator {

    public X509Certificate generateCertificate(CertificateConfig config) throws CertificateException, OperatorCreationException, CertIOException, NoSuchAlgorithmException {
        X509v3CertificateBuilder certificateBuilder = createCertificateBuilder(config);
        addExtensions(certificateBuilder, config);
        ContentSigner contentSigner = createContentSigner(config);
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

    private X509v3CertificateBuilder createCertificateBuilder(CertificateConfig config) {
        X500Name issuer, subject;
        if (config.getIssuer() == null) {
            issuer = new X500Name("CN=localhost");
            subject = new X500Name("CN=localhost");
        } else {
            issuer = new X500Name("CN=" + config.getIssuer().getUserEmail());
            subject = config.getSubject();
        }

        Date validFrom = config.getValidFrom();
        Date validTo = config.getValidTo();
        KeyPair keyPair = config.getKeyPair();

        return new JcaX509v3CertificateBuilder(
                issuer,
                config.getSerialNumber(),
                validFrom,
                validTo,
                subject,
                keyPair.getPublic());
    }

    private void addExtensions(X509v3CertificateBuilder certificateBuilder, CertificateConfig config) throws CertIOException, NoSuchAlgorithmException {
        certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(config.isAuthority()));
        certificateBuilder.addExtension(Extension.keyUsage, true, config.getKeyUsage());
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(config.getKeyPair().getPublic().getEncoded());
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, new JcaX509ExtensionUtils().createSubjectKeyIdentifier(publicKeyInfo));

        if (config.getIssuerCertificate() != null) {
            SubjectPublicKeyInfo issuerPublicKeyInfo = SubjectPublicKeyInfo.getInstance(config.getIssuerCertificate().getPublicKey().getEncoded());
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(issuerPublicKeyInfo));
        }
    }

    private ContentSigner createContentSigner(CertificateConfig config) throws OperatorCreationException {
        return new JcaContentSignerBuilder(config.getSignatureAlgorithm()).build(config.getSigningKey());
    }
}
