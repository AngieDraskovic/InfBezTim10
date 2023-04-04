package com.example.InfBezTim10.dto;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateType;
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
public class CertificateBasicDTO {

    private String userEmail;
    private CertificateType type;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private Date validFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private Date validTo;

    public CertificateBasicDTO(Certificate certificate){
        this.userEmail = certificate.getUserEmail();
        this.type = certificate.getType();
        this.validFrom = certificate.getValidFrom();
        this.validTo = certificate.getValidTo();
    }
}
