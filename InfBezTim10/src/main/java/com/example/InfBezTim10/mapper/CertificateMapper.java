package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.CertificateBasicDTO;
import com.example.InfBezTim10.model.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CertificateMapper {
    CertificateMapper INSTANCE = Mappers.getMapper(CertificateMapper.class);

    CertificateBasicDTO certificateToCertificateBasicDTO(Certificate certificate);

}
