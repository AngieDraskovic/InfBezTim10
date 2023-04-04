package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.implementation.CertificateRequestService;
import com.example.InfBezTim10.service.implementation.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/certificate/request")
public class CertificateRequestController {
    private CertificateService certificateGenerator;
    private CertificateRequestService certificateRequestService;

    @Autowired
    public CertificateRequestController(CertificateService certificateGenerator, CertificateRequestService certificateRequestService) {
        this.certificateGenerator = certificateGenerator;
        this.certificateRequestService = certificateRequestService;
    }





}
