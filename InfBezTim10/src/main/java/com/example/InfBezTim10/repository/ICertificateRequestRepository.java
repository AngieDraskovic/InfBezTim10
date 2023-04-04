package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.CertificateRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICertificateRequestRepository extends MongoRepository<CertificateRequest, String> {

}
