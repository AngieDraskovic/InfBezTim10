package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.certificateRequst.CreateCertificateRequestDTO;
import com.example.InfBezTim10.dto.RejectionReasonDTO;
import com.example.InfBezTim10.exception.certificate.CertificateGenerationException;
import com.example.InfBezTim10.mapper.CertificateRequestMapper;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateRequest;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.certificateManagement.implementation.CertificateRequestService;
import com.example.InfBezTim10.service.certificateManagement.implementation.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/certificate/request")
public class CertificateRequestController {
    private ICertificateService certificateService;
    private CertificateRequestService certificateRequestService;

    public CertificateRequestController(CertificateService certificateService, CertificateRequestService certificateRequestService) {
        this.certificateService = certificateService;
        this.certificateRequestService = certificateRequestService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createUserCertificateRequest(@RequestBody CreateCertificateRequestDTO createCertificateRequestDTO, Principal principal) {
        try {
            CertificateRequest certificateRequest = CertificateRequestMapper.INSTANCE.createCertificateRequestDTOToCertificateRequest(createCertificateRequestDTO);
            certificateRequest.setSubjectUsername(principal.getName());
            return ResponseEntity.ok(certificateRequestService.createCertificateRequest(certificateRequest, "ROLE_USER", principal.getName()));
        } catch (CertificateGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createAdminCertificateRequest(@RequestBody CreateCertificateRequestDTO createCertificateRequestDTO, Principal principal) {
        try {
            CertificateRequest certificateRequest = CertificateRequestMapper.INSTANCE.createCertificateRequestDTOToCertificateRequest(createCertificateRequestDTO);
            certificateRequest.setSubjectUsername(principal.getName());
            CertificateRequest certificateRequest1 = certificateRequestService.createCertificateRequest(certificateRequest, "ROLE_ADMIN", principal.getName());
            return ResponseEntity.ok(certificateRequest1);
        } catch (CertificateGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/outgoing-requests")
    public ResponseEntity<List<CertificateRequest>> getOutgoingRequestsForUser(Principal principal) {
        return ResponseEntity.ok(certificateRequestService.getOutgoingRequestsForUser(principal.getName()));
    }

    @GetMapping("/pending-incoming-requests")
    public ResponseEntity<List<CertificateRequest>> getPendingIncomingRequestsForUser(Principal principal) {
        return ResponseEntity.ok(certificateRequestService.getPendingIncomingRequestsForUser(principal.getName()));
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<Certificate> approveCertificateRequest(@PathVariable String requestId, Principal principal) {
        try {
            return ResponseEntity.ok(certificateRequestService.approveCertificateRequest(requestId, principal.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<Void> rejectCertificateRequest(
            @PathVariable String requestId,
            @RequestBody RejectionReasonDTO rejectionReasonDTO,
            Principal principal
    ) {
        certificateRequestService.rejectCertificateRequest(requestId, principal.getName(), rejectionReasonDTO.getReason());
        return ResponseEntity.noContent().build();
    }


}
