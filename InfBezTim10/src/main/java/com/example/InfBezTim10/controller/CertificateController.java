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
import java.util.Arrays;
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", serialNumber + ".crt");

        X509Certificate certificate = certificateFileUtils.readCertificate(serialNumber);
        try {
            return new ResponseEntity<>(certificate.getEncoded(), headers, HttpStatus.OK);
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}/download-private-key")
    public ResponseEntity<?> downloadPrivateKey(@PathVariable("serialNumber") String serialNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", serialNumber + ".key");

        PrivateKey certificate = certificateFileUtils.readPrivateKey(serialNumber);
        return new ResponseEntity<>(certificate.getEncoded(), headers, HttpStatus.OK);
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
    @GetMapping("/paginated")
    public ResponseEntity<PaginatedResponse<CertificateDTO>> getAllPaged(Pageable pageable) {
        Page<Certificate> certificatesPage = certificateService.findAll(pageable);
        PaginatedResponse<CertificateDTO> response = pageToPaginatedResponse(certificatesPage, CertificateMapper.INSTANCE::certificateToCertificateDTO);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/owner")
    public ResponseEntity<PaginatedResponse<CertificateDTO>> getAllForUser(Principal principal, Pageable pageable) {
        Page<Certificate> certificatesPage = certificateService.findCertificatesForUser(principal.getName(), pageable);
        PaginatedResponse<CertificateDTO> response = pageToPaginatedResponse(certificatesPage, CertificateMapper.INSTANCE::certificateToCertificateDTO);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}")
    public ResponseEntity<CertificateDTO> getBySerialNumber(@PathVariable String serialNumber) {
        Certificate certificate = certificateService.findBySerialNumber(serialNumber);
        CertificateDTO dto = CertificateMapper.INSTANCE.certificateToCertificateDTO(certificate);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{serialNumber}/validate")
    public ResponseEntity<?> validate(@PathVariable("serialNumber") String serialNumber) {
        certificateValidationService.validate(serialNumber);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping(value = "/validateCopy")
    public ResponseEntity<?> validateCopy(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        long MAX_FILE_SIZE = 5 * 1024 * 1024;
        if (file.getSize() > MAX_FILE_SIZE) {
            return new ResponseEntity<>("File is too large. Maximum size is 5MB.", HttpStatus.BAD_REQUEST);
        }

        String[] acceptableFileTypes = {"application/x-x509-ca-cert", "application/x-pem-file", "application/pkix-cert"};
        String fileType = file.getContentType();
        if (!Arrays.asList(acceptableFileTypes).contains(fileType)) {
            return new ResponseEntity<>("Invalid file type. Only .crt, .pem, .cer, or .key files are allowed.", HttpStatus.BAD_REQUEST);
        }

        certificateValidationService.validate(file);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping(value = "/{serialNumber}/revoke")
    public ResponseEntity<?> revokeCertificate(@PathVariable("serialNumber") String serialNumber) {
        certificateService.revokeCertificate(serialNumber);
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
