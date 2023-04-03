package com.example.InfBezTim10.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.security.auth.x500.X500Principal;

public class X509CertificateGeneratorUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate copyWithPrivateKey(X509Certificate certificate, PrivateKey privateKey)
            throws CertificateException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(certificate.getPublicKey().getEncoded()));
            X509Certificate newCertificate = (X509Certificate) CertificateFactory.getInstance("X.509", "BC")
                    .generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
            newCertificate.verify(publicKey);
            return newCertificate;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | InvalidKeyException
                 | SignatureException e) {
            throw new CertificateException("Error while copying the certificate with private key", e);
        }
    }

    public static X509Certificate signCertificate(X500Principal issuer, X500Principal subject, BigInteger serialNumber,
                                                  PublicKey publicKey, PrivateKey privateKey, Date notBefore, Date notAfter,
                                                  boolean isCertificateAuthority, KeyUsage keyUsage) throws CertificateException {

        try {
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(privateKey);

            JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                    issuer, serialNumber, notBefore, notAfter, subject, publicKey);

            certificateBuilder.addExtension(Extension.basicConstraints, true,
                    new BasicConstraints(isCertificateAuthority));
            certificateBuilder.addExtension(Extension.keyUsage, true, keyUsage);

            org.bouncycastle.cert.X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);

            return new JcaX509CertificateConverter()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(certificateHolder);
        } catch (OperatorCreationException | CertIOException e) {
            throw new CertificateException("Error creating content signer", e);
        }
    }

}

