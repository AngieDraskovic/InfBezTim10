package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.dto.RejectionReasonDTO;
import com.example.InfBezTim10.dto.ResponseMessageDTO;
import com.example.InfBezTim10.dto.UserDetailsDTO;
import com.example.InfBezTim10.exception.CertificateGenerationException;
import com.example.InfBezTim10.exception.EmailAlreadyExistsException;
import com.example.InfBezTim10.mapper.UserMapper;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateRequest;
import com.example.InfBezTim10.model.User;
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
    public ResponseEntity<?> createUserCertificateRequest(@RequestBody CertificateRequestDTO certificateRequestDTO, Principal principal) {
        try {
            return ResponseEntity.ok(certificateRequestService.createCertificateRequest(certificateRequestDTO, "ROLE_USER", principal.getName()));
        } catch (CertificateGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createAdminCertificateRequest(@RequestBody CertificateRequestDTO certificateRequestDTO, Principal principal) {
        try {
            return ResponseEntity.ok(certificateRequestService.createCertificateRequest(certificateRequestDTO, "ROLE_ADMIN", principal.getName()));
        } catch (CertificateGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/user-requests")
    public ResponseEntity<List<CertificateRequest>> getCertificateRequestsByUser(Principal principal) {
        return ResponseEntity.ok(certificateRequestService.getCertificateRequestsByUser(principal.getName()));
    }

    @PutMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Certificate> approveCertificateRequest(@PathVariable String requestId, Principal principal) {
        try {
            return ResponseEntity.ok(certificateRequestService.approveCertificateRequest(requestId, principal.getName()));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> rejectCertificateRequest(
            @PathVariable String requestId,
            @RequestBody RejectionReasonDTO rejectionReasonDTO,
            Principal principal
    ) {
        certificateRequestService.rejectCertificateRequest(requestId, principal.getName(), rejectionReasonDTO.getReason());
        return ResponseEntity.noContent().build();
    }



}
