package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.certificate.CertificateRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICertificateRequestRepository extends MongoRepository<CertificateRequest, String> {
        List<CertificateRequest> findBySubjectUsername(String username);

}
