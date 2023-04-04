package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.ICertificateGeneratorService;
import com.example.InfBezTim10.service.implementation.CertificateService;
import com.mongodb.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    ICertificateGeneratorService certificateGeneratorService;

    @Autowired
    public CertificateController(ICertificateGeneratorService certificateGeneratorService) {
        this.certificateGeneratorService = certificateGeneratorService;
    }

    @PostMapping(value = "/issueCertificate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Certificate> issueCertificate(@RequestBody CertificateRequestDTO certificateRequestDTO) {
        try {
            Certificate certificate = certificateGeneratorService.issueCertificate(
                    certificateRequestDTO.getIssuerSN(),
                    certificateRequestDTO.getSubjectUsername(),
                    certificateRequestDTO.getKeyUsageFlags(),
                    certificateRequestDTO.getValidTo());
            return new ResponseEntity<>(certificate, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
