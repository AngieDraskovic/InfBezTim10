package com.example.InfBezTim10.service.implementation;
import com.example.InfBezTim10.dto.CertificateRequestDTO;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.*;
import com.example.InfBezTim10.repository.ICertificateRequestRepository;
import com.example.InfBezTim10.service.ICertificateRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CertificateRequestService extends MongoService<CertificateRequest>  implements ICertificateRequestService {

    private final ICertificateRequestRepository certificateRequestRepository;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository) {
        this.certificateRequestRepository = certificateRequestRepository;
    }


    @Override
    protected MongoRepository<CertificateRequest, String> getEntityRepository() {
        return this.certificateRequestRepository;
    }
}
