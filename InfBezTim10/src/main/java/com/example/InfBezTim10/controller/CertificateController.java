package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.AuthTokenDTO;
import com.example.InfBezTim10.dto.ResponseMessageDTO;
import com.example.InfBezTim10.dto.UserCredentialsDTO;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.repository.ICertificateRepository;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.implementation.CertificateService;
import com.example.InfBezTim10.service.implementation.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    ICertificateRepository certificateRepository;
    CertificateService certificateGenerator;

    @Autowired
    public CertificateController(CertificateService certificateGenerator) {
        this.certificateGenerator = certificateGenerator;
    }


    @GetMapping(value = "/foo")
    public ResponseEntity<?> foo() {

        try {
            // Replace with your test parameters
            String issuerSN = null;
            String subjectUsername = "admin@gmail.com";
            String keyUsageFlags = "0,1,2,3,4,5,6,7,8";
            Date validTo = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());

            Certificate generatedCertificate = certificateGenerator.issueCertificate(issuerSN, subjectUsername, keyUsageFlags, validTo);

            System.out.println("Generated certificate:");
            System.out.println(generatedCertificate);


        } catch (Exception e) {
            e.printStackTrace();
        }



        return ResponseEntity.status(HttpStatus.OK).body("bar");

    }
}
