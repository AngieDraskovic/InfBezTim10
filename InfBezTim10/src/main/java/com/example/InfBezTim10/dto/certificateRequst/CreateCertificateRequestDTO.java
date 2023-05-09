package com.example.InfBezTim10.dto.certificateRequst;

import com.example.InfBezTim10.model.certificate.CertificateType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateCertificateRequestDTO {
    String issuerSN;
    CertificateType certificateType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+02:00")
    Date validTo;
}
