package com.example.InfBezTim10.dto.certificateRequst;

import com.example.InfBezTim10.model.certificate.CertificateRequestStatus;
import com.example.InfBezTim10.model.certificate.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateRequestDTO {
    private String id;
    private String issuerSN;
    private String subjectUsername;
    private String keyUsageFlags;
    private Date validTo;
    private CertificateRequestStatus status;
    private String reason;
    private CertificateType certificateType;
}