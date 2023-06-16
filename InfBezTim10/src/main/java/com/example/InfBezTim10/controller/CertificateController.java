package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.PaginatedResponse;
import com.example.InfBezTim10.dto.certificate.CertificateDTO;
import com.example.InfBezTim10.mapper.CertificateMapper;
import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import com.example.InfBezTim10.service.certificateManagement.ICertificateGeneratorService;
import com.example.InfBezTim10.service.certificateManagement.ICertificateService;
import com.example.InfBezTim10.service.certificateManagement.ICertificateValidationService;
import com.example.InfBezTim10.utils.CertificateFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    private final ICertificateService certificateService;
    private final ICertificateValidationService certificateValidationService;
    private final ICertificateGeneratorService certificateGeneratorService;
    private final CertificateFileUtils certificateFileUtils;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public CertificateController(ICertificateService certificateService, ICertificateValidationService certificateValidationService, ICertificateGeneratorService certificateGeneratorService, CertificateFileUtils certificateFileUtils) {
        this.certificateService = certificateService;
        this.certificateValidationService = certificateValidationService;
        this.certificateGeneratorService = certificateGeneratorService;
        this.certificateFileUtils = certificateFileUtils;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}/download-certificate")
    public ResponseEntity<?> downloadCertificate(@PathVariable("serialNumber") String serialNumber) {
        logger.info("Starting certificate download for serial number: {}", serialNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", serialNumber + ".crt");

        X509Certificate certificate = certificateFileUtils.readCertificate(serialNumber);
        logger.info("Successful certificate download for serial number: {}", serialNumber);
        try {
            return new ResponseEntity<>(certificate.getEncoded(), headers, HttpStatus.OK);
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}/download-private-key")
    public ResponseEntity<?> downloadPrivateKey(@PathVariable("serialNumber") String serialNumber) {
        logger.info("Starting private key download for serial number: {}", serialNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", serialNumber + ".key");

        PrivateKey certificate = certificateFileUtils.readPrivateKey(serialNumber);
        logger.info("Successful private key download for serial number: {}", serialNumber);
        return new ResponseEntity<>(certificate.getEncoded(), headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping()
    public ResponseEntity<List<CertificateDTO>> getAll() {
        logger.info("Getting all certificates");
        List<Certificate> certificateList = certificateService.findAll();
        List<CertificateDTO> certificateDTOS = certificateList.stream()
                .map(CertificateMapper.INSTANCE::certificateToCertificateDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(certificateDTOS);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/paginated")
    public ResponseEntity<PaginatedResponse<CertificateDTO>> getAllPaged(Pageable pageable) {
        logger.info("Getting all certificates paged");
        Page<Certificate> certificatesPage = certificateService.findAll(pageable);
        PaginatedResponse<CertificateDTO> response = pageToPaginatedResponse(certificatesPage, CertificateMapper.INSTANCE::certificateToCertificateDTO);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/owner")
    public ResponseEntity<PaginatedResponse<CertificateDTO>> getAllForUser(Principal principal, Pageable pageable) {
        logger.info("Getting all user certificates");
        Page<Certificate> certificatesPage = certificateService.findCertificatesForUser(principal.getName(), pageable);
        PaginatedResponse<CertificateDTO> response = pageToPaginatedResponse(certificatesPage, CertificateMapper.INSTANCE::certificateToCertificateDTO);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}")
    public ResponseEntity<CertificateDTO> getBySerialNumber(@PathVariable String serialNumber) {
        logger.info("Getting certificate by user email" +
                "");
        Certificate certificate = certificateService.findBySerialNumber(serialNumber);
        CertificateDTO dto = CertificateMapper.INSTANCE.certificateToCertificateDTO(certificate);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}/validate")
    public ResponseEntity<?> validate(@PathVariable("serialNumber") String serialNumber) {
        logger.info("Validating certificate with serial number: " + serialNumber );
        certificateValidationService.validate(serialNumber);
        logger.info("Certficate succesfully validated: " + serialNumber );
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping(value = "/validateCopy")
    public ResponseEntity<?> validateCopy(@RequestParam("file") MultipartFile file) {
        logger.info("Validating copy");
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        certificateValidationService.validate(file);
        logger.info("Validation successfully completed.");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping(value = "/{serialNumber}/revoke")
    public ResponseEntity<?> revokeCertificate(@PathVariable("serialNumber") String serialNumber) {
        logger.info("Revoking certificate with serial number: " + serialNumber );
        certificateService.revokeCertificate(serialNumber);
        logger.info("Certificate with serial number: " + serialNumber  + " revoked");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/count/all")
    public ResponseEntity<Long> countAllCertificates() {
        long count = certificateService.countAllCertificates();
        return ResponseEntity.ok(count);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/count/status")
    public ResponseEntity<Long> countCertificatesByStatus(@RequestParam CertificateStatus status) {
        Long count = certificateService.countCertificatesByStatus(status);
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
