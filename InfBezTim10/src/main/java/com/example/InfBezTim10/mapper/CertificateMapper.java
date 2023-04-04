package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.CertificateDTO;
import com.example.InfBezTim10.model.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CertificateMapper {
    CertificateMapper INSTANCE = Mappers.getMapper(CertificateMapper.class);

    CertificateDTO certificateToCertificateDTO(Certificate certificate);

}
