package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateRequest;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.service.implementation.CertificateRequestService;
import com.example.InfBezTim10.service.implementation.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/certificate/request")
public class CertificateRequestController {
    private CertificateService certificateGenerator;
    private CertificateRequestService certificateRequestService;

    public CertificateRequestController(CertificateService certificateGenerator, CertificateRequestService certificateRequestService) {
        this.certificateGenerator = certificateGenerator;
        this.certificateRequestService = certificateRequestService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createUserCertificateRequest(@RequestBody CertificateRequestDTO certificateRequestDTO) {

        return ResponseEntity.ok(certificateRequestService.createCertificateRequest(certificateRequestDTO, "USER"));
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createAdminCertificateRequest(@RequestBody CertificateRequestDTO certificateRequestDTO) {
        return ResponseEntity.ok(certificateRequestService.createCertificateRequest(certificateRequestDTO, "ADMIN"));
    }




}
