package com.example.InfBezTim10.service;

import com.example.InfBezTim10.exception.CertificateValidationException;
import org.springframework.web.multipart.MultipartFile;

public interface ICertificateValidationService {
    void validate(MultipartFile file) throws CertificateValidationException;

    void validate(String certSN) throws CertificateValidationException;
}
