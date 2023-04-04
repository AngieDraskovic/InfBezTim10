package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.CertificateDTO;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.dto.ResponseMessageDTO;
import com.example.InfBezTim10.exception.CertificateNotFoundException;
import com.example.InfBezTim10.mapper.CertificateMapper;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.service.ICertificateGeneratorService;
import com.example.InfBezTim10.service.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    ICertificateService certificateService;
    ICertificateGeneratorService certificateGeneratorService;

    @Autowired
    public CertificateController(ICertificateService certificateService, ICertificateGeneratorService certificateGeneratorService) {
        this.certificateService = certificateService;
        this.certificateGeneratorService = certificateGeneratorService;
    }

    @PostMapping(value = "/issueCertificate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateDTO> issueCertificate(@RequestBody CertificateRequestDTO certificateRequestDTO) {
        try {
            Certificate certificate = certificateGeneratorService.issueCertificate(
                    certificateRequestDTO.getIssuerSN(),
                    certificateRequestDTO.getSubjectUsername(),
                    certificateRequestDTO.getKeyUsageFlags(),
                    certificateRequestDTO.getValidTo());
            return new ResponseEntity<>(CertificateMapper.INSTANCE.certificateToCertificateDTO(certificate), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
    @GetMapping(value="/validate/{serialNumber}")
    public ResponseEntity<?> validate(@PathVariable("serialNumber") String serialNumber){
        try {
            boolean isValid = certificateService.validate(serialNumber);
            return ResponseEntity.status(HttpStatus.OK).body(isValid);
        }catch (CertificateNotFoundException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

}
