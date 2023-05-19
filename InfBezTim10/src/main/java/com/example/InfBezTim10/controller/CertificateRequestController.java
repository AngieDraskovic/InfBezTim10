package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.PaginatedResponse;
import com.example.InfBezTim10.dto.certificateRequst.CertificateRequestDTO;
import com.example.InfBezTim10.dto.certificateRequst.CreateCertificateRequestDTO;
import com.example.InfBezTim10.dto.RejectionReasonDTO;
import com.example.InfBezTim10.exception.certificate.CertificateGenerationException;
import com.example.InfBezTim10.mapper.CertificateRequestMapper;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateRequest;
import com.example.InfBezTim10.model.certificate.CertificateRequestStatus;
import com.example.InfBezTim10.service.certificateManagement.ICertificateRequestStatisticsService;
import com.example.InfBezTim10.service.certificateManagement.implementation.CertificateRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate/request")
public class CertificateRequestController {
    private final CertificateRequestService certificateRequestService;
    private final ICertificateRequestStatisticsService certificateRequestStatisticsService;

    public CertificateRequestController(CertificateRequestService certificateRequestService, ICertificateRequestStatisticsService certificateRequestStatisticsService) {
        this.certificateRequestService = certificateRequestService;
        this.certificateRequestStatisticsService = certificateRequestStatisticsService;
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

    @GetMapping()
    public ResponseEntity<PaginatedResponse<CertificateRequestDTO>> getAll(Pageable pageable) {
        Page<CertificateRequest> certificateRequestsPage = certificateRequestService.findAll(pageable);
        PaginatedResponse<CertificateRequestDTO> response = pageToPaginatedResponse(certificateRequestsPage, CertificateRequestMapper.INSTANCE::certificateRequestToCertificateRequestDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/outgoing-requests")
    public ResponseEntity<PaginatedResponse<CertificateRequestDTO>> getOutgoingRequestsForUser(Principal principal, Pageable pageable) {
        Page<CertificateRequest> certificateRequestsPage = certificateRequestService.getByUsername(principal.getName(), pageable);
        PaginatedResponse<CertificateRequestDTO> response = pageToPaginatedResponse(certificateRequestsPage, CertificateRequestMapper.INSTANCE::certificateRequestToCertificateRequestDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-incoming-requests")
    public ResponseEntity<PaginatedResponse<CertificateRequestDTO>> getIncomingRequestsForUser(Principal principal, Pageable pageable) {
        Page<CertificateRequest> certificateRequestsPage = certificateRequestService.getByStatusAndUsername(CertificateRequestStatus.PENDING, principal.getName(), pageable);
        PaginatedResponse<CertificateRequestDTO> response = pageToPaginatedResponse(certificateRequestsPage, CertificateRequestMapper.INSTANCE::certificateRequestToCertificateRequestDTO);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/count/all")
    public ResponseEntity<Long> countAllCertificateRequests() {
        long count = certificateRequestStatisticsService.countAllCertificateRequests();
        return ResponseEntity.ok(count);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/count/user")
    public ResponseEntity<Long> countAllCertificateRequestsByUser(Principal principal) {
        Long count = certificateRequestStatisticsService.countAllCertificateRequestsByUser(principal.getName());
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/count/status")
    public ResponseEntity<Long> countCertificateRequestsByStatus(@RequestParam CertificateRequestStatus status) {
        Long count = certificateRequestStatisticsService.countCertificateRequestsByStatus(status);
        return ResponseEntity.ok(count);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/count/user-status")
    public ResponseEntity<Long> countCertificateRequestsByStatusAndUser(@RequestParam CertificateRequestStatus status, Principal principal) {
        Long count = certificateRequestStatisticsService.countAllCertificateRequestsByStatusAndUser(status, principal.getName());
        return ResponseEntity.ok(count);
    }

    private <T, U> PaginatedResponse<U> pageToPaginatedResponse(Page<T> page, Function<T, U> mapper) {
        return new PaginatedResponse<>(
                page.getContent().stream().map(mapper).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
