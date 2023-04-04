package com.example.InfBezTim10.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateConfig {
    private Certificate issuer;
    private X500Name subject;
    private boolean isAuthority;
    private BigInteger serialNumber;
    private Date validFrom;
    private Date validTo;
    private KeyPair keyPair;
    private X509Certificate issuerCertificate;
    private KeyUsage keyUsage;
    private String signatureAlgorithm;
    private PrivateKey signingKey;
}
