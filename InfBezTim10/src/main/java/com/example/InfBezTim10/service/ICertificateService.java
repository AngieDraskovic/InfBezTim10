package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.cert.CertificateException;

public interface ICertificateService extends IJPAService<Certificate>{
    Certificate findBySerialNumber(String serialNumber);
}
