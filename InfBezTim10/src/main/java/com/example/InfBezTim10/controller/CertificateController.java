package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.CertificateDTO;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.dto.ResponseMessageDTO;
import com.example.InfBezTim10.exception.CertificateNotFoundException;
import com.example.InfBezTim10.mapper.CertificateMapper;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.service.ICertificateGeneratorService;
import com.example.InfBezTim10.service.ICertificateService;
import com.example.InfBezTim10.utils.CertificateFileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    ICertificateService certificateService;
    ICertificateGeneratorService certificateGeneratorService;
    CertificateFileUtils certificateFileUtils;

    @Autowired
    public CertificateController(ICertificateService certificateService, ICertificateGeneratorService certificateGeneratorService, CertificateFileUtils certificateFileUtils) {
        this.certificateService = certificateService;
        this.certificateGeneratorService = certificateGeneratorService;
        this.certificateFileUtils = certificateFileUtils;
    }

    @PostMapping(value = "/issueCertificate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> issueCertificate(@RequestBody CertificateRequestDTO certificateRequestDTO) {
        try {
            Certificate certificate = certificateGeneratorService.issueCertificate(
                    certificateRequestDTO.getIssuerSN(),
                    certificateRequestDTO.getSubjectUsername(),
                    certificateRequestDTO.getKeyUsageFlags(),
                    certificateRequestDTO.getValidTo());
            return new ResponseEntity<>(CertificateMapper.INSTANCE.certificateToCertificateDTO(certificate), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/downloadCertificate/{serialNumber}")
    public ResponseEntity<?> downloadCertificate(@PathVariable("serialNumber") String serialNumber) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", serialNumber + ".crt");

            X509Certificate certificate = certificateFileUtils.readCertificate(serialNumber);
            return new ResponseEntity<>(certificate.getEncoded(), headers, HttpStatus.OK);
        } catch (CertificateException | IOException | CertificateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.OK).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping()
    public ResponseEntity<List<CertificateDTO>> getAll() {
        List<Certificate> certificateList = certificateService.findAll();
        List<CertificateDTO> certificateDTOS = certificateList.stream()
                .map(CertificateMapper.INSTANCE::certificateToCertificateDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(certificateDTOS);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/validate/{serialNumber}")
    public ResponseEntity<?> validate(@PathVariable("serialNumber") String serialNumber) {
        try {
            boolean isValid = certificateService.validate(serialNumber);
            return ResponseEntity.status(HttpStatus.OK).body(isValid);
        } catch (CertificateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/validateCopy")
    public ResponseEntity<?> validateCopy(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            boolean isValid = certificateService.validate(file);
            return ResponseEntity.status(HttpStatus.OK).body(isValid);
        } catch (CertificateNotFoundException | CertificateException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }
}
