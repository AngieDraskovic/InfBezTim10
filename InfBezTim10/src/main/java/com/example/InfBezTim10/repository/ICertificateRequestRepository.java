package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.certificate.CertificateRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICertificateRequestRepository extends MongoRepository<CertificateRequest, String> {

    @Query("{'subjectUsername': ?0}")
    List<CertificateRequest> findBySubjectUsername(String username);

    @Query("{ 'issuerSN' : { $in: ?0 }, 'status' : 'PENDING' }")
    List<CertificateRequest> findPendingIncomingRequestsByIssuerSerialNumbers(List<String> issuerSerialNumbers);
}
