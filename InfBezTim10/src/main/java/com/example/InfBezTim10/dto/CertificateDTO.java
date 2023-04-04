package com.example.InfBezTim10.dto;

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
public class CertificateDTO {
    private String userEmail;

    private CertificateType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+02:00")
    private Date validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+02:00")
    private Date validTo;
}
