package com.example.InfBezTim10.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "certificates")
@Getter
@Setter
public class Certificate extends BaseEntity {
    @Id
    public String id;
    @Indexed(unique = true)
    public String SerialNumber;
    public String SignatureAlgorithm;
    public String Issuer;
    public LocalDate validFrom;
    public LocalDate validTo;
    public CertificateStatus status;
    public CertificateType type;
    // user associated with certificate
    public String userEmail;
}
