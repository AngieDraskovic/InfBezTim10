package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.model.certificate.CertificateRequest;
import com.example.InfBezTim10.service.base.IJPAService;

import java.util.List;

public interface ICertificateRequestService extends IJPAService<CertificateRequest> {
    List<CertificateRequest> getOutgoingRequestsForUser(String username);

    List<CertificateRequest> getPendingIncomingRequestsForUser(String username);
}
