package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.CertificateBasicDTO;
import com.example.InfBezTim10.mapper.CertificateMapper;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.implementation.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    ICertificateRepository certificateRepository;
    CertificateService certificateGenerator;

    @Autowired
    public CertificateController(CertificateService certificateGenerator) {
        this.certificateGenerator = certificateGenerator;
    }


    @GetMapping(value = "/foo")
    public ResponseEntity<?> foo() {

        try {
            // Replace with your test parameters
            String issuerSN = "f741df8de1f22c77";
            String subjectUsername = "peraperic@gmail.com";
            String keyUsageFlags = "0,1,2,3,4,5,6,7,8";
            Date validTo = Date.from(LocalDateTime.now().plusDays(10).atZone(ZoneId.systemDefault()).toInstant());

            Certificate generatedCertificate = certificateGenerator.issueCertificate(issuerSN, subjectUsername, keyUsageFlags, validTo);

            System.out.println("Generated certificate:");
            System.out.println(generatedCertificate);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.OK).body("bar");

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping()
    public ResponseEntity<List<CertificateBasicDTO>> getAll() {
        List<Certificate> certificateList = certificateGenerator.findAll();
        List<CertificateBasicDTO> certificateBasicDTOS = certificateList.stream()
                .map(CertificateMapper.INSTANCE::certificateToCertificateBasicDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(certificateBasicDTOS);
    }


//     @GetMapping(value="/validate/{serialNumber}")
//     public ResponseEntity<?> validate(@PathVariable("serialNumber")BigInteger serialNumber){
//
//
//    }

}
