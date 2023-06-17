package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.model.certificate.CertificateRequest;
import com.example.InfBezTim10.model.certificate.CertificateRequestStatus;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.service.base.IJPAService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICertificateRequestService extends IJPAService<CertificateRequest> {
    Page<CertificateRequest> getByUsername(String username, Pageable pageable);

    Page<CertificateRequest> getByStatusAndUsername(CertificateRequestStatus status, String userEmail, Pageable pageable);
}
