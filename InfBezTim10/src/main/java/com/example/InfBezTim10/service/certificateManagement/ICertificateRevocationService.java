package com.example.InfBezTim10.service.certificateManagement;

import com.example.InfBezTim10.exception.certificate.CertificateRevocationException;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.user.Authority;

public interface ICertificateRevocationService {


    void revoke(String certSN, String userEmail) throws CertificateRevocationException;

    void revoke(Certificate cert) throws CertificateRevocationException;
}
