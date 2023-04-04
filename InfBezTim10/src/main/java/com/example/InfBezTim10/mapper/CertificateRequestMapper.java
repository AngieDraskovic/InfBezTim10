package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.CertificateDTO;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CertificateRequestMapper {
    CertificateRequestMapper INSTANCE = Mappers.getMapper(CertificateRequestMapper.class);

    CertificateRequest certificateRequestDTOToCertificateRequest(CertificateRequestDTO certificateRequestDTO);

}
