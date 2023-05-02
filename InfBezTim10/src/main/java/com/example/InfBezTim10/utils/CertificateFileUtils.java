package com.example.InfBezTim10.utils;

import com.example.InfBezTim10.exception.certificate.CertificateNotFoundException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public CertificateFileUtils(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public void writeCertificate(X509Certificate certificate, String serialNumber) throws CertificateEncodingException {
        byte[] fileContent = certificate.getEncoded();
        String fileName = serialNumber + ".crt";
        String contentType = "application/x-x509-ca-cert";
        gridFsTemplate.store(new ByteArrayInputStream(fileContent), fileName, contentType);
    }

    public X509Certificate readCertificate(String serialNumber) throws IOException, CertificateException {
        String fileName = serialNumber + ".crt";
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("filename").is(fileName)));
        if (gridFSFile != null) {
            GridFsResource gridFsResource = gridFsTemplate.getResource(gridFSFile);
            InputStream inputStream = gridFsResource.getInputStream();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inputStream);
        }

        throw new CertificateNotFoundException("Certificate is not found!");
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

    public X509Certificate convertMultipartFileToX509Certificate(MultipartFile file) throws IOException, CertificateException {
        InputStream inputStream = file.getInputStream();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        inputStream.close();
        return x509Certificate;
    }
}
