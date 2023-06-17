package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.certificate.CertificateDTO;
import com.example.InfBezTim10.model.certificate.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CertificateMapper {
    CertificateMapper INSTANCE = Mappers.getMapper(CertificateMapper.class);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "type", source = "type")
    CertificateDTO certificateToCertificateDTO(Certificate certificate);

}
