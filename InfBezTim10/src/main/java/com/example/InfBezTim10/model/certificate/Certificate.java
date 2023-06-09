package com.example.InfBezTim10.model.certificate;

import com.example.InfBezTim10.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Date;

@Document(collection = "certificates")
@Getter
@Setter
public class Certificate extends BaseEntity {

    @Indexed(unique = true)
    public String serialNumber;
    public String signatureAlgorithm;
    public String issuer;
    public Date validFrom;
    public Date validTo;
    public CertificateStatus status;
    public CertificateType type;
    public String userEmail;
}
