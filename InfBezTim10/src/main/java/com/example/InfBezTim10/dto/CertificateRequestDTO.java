package com.example.InfBezTim10.dto;

import com.example.InfBezTim10.model.CertificateType;
import com.example.InfBezTim10.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateRequestDTO {
    private String issuerSN;
    private String subjectUsername;
    private String keyUsageFlags;
    private String validTo;
    private CertificateType certificateType;

}
