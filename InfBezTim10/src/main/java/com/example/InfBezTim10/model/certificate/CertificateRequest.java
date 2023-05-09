package com.example.InfBezTim10.model.certificate;

import com.example.InfBezTim10.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document(collection = "certificate_requests")
@Getter
@Setter
public class CertificateRequest extends BaseEntity {

    @Indexed(unique = true)
    private String issuerSN;
    private String subjectUsername;
    private CertificateType certificateType;
    private Date validTo;
    private String keyUsageFlags;
    private CertificateRequestStatus status;
    private String rejectionReason;
}
