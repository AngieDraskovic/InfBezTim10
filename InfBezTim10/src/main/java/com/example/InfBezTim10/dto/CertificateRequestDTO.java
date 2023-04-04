package com.example.InfBezTim10.dto;

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
    String issuerSN;
    String subjectUsername;
    String keyUsageFlags;
    Date validTo;
}
