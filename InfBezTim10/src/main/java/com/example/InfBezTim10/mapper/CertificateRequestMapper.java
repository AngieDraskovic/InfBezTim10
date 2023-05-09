package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.certificateRequst.CreateCertificateRequestDTO;
import com.example.InfBezTim10.model.certificate.CertificateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CertificateRequestMapper {
    CertificateRequestMapper INSTANCE = Mappers.getMapper(CertificateRequestMapper.class);

    CertificateRequest createCertificateRequestDTOToCertificateRequest(CreateCertificateRequestDTO createCertificateRequestDTO);

    CertificateRequest certificateToCertificateRequestDTO(CertificateRequest certificateRequest);

}
