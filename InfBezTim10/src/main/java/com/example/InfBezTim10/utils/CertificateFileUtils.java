package com.example.InfBezTim10.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class CertificateFileUtils {

    private static final String CERT_DIR = "certs";

    public static X509Certificate readCertificate(String serialNumber) throws IOException, CertificateException {
        Path certificatePath = Paths.get(CERT_DIR, serialNumber + ".crt");
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(Files.newInputStream(certificatePath));
    }

    public static void writeCertificate(X509Certificate certificate, String serialNumber) throws IOException, CertificateEncodingException {
        Path certificatePath = Paths.get(CERT_DIR, serialNumber + ".crt");
        Files.write(certificatePath, certificate.getEncoded());
    }

    public static PrivateKey readPrivateKey(String serialNumber) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path privateKeyPath = Paths.get(CERT_DIR, serialNumber + ".key");
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static void writePrivateKey(PrivateKey privateKey, String serialNumber) throws IOException {
        Path privateKeyPath = Paths.get(CERT_DIR, serialNumber + ".key");
        Files.write(privateKeyPath, privateKey.getEncoded());
    }
}
