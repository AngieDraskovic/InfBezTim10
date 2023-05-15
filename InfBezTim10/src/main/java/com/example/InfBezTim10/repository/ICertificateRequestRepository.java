package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.certificate.CertificateRequest;
import com.example.InfBezTim10.model.certificate.CertificateRequestStatus;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICertificateRequestRepository extends MongoRepository<CertificateRequest, String> {

    @Query("{'subjectUsername': ?0}")
    Page<CertificateRequest> findBySubjectUsername(String username, Pageable pageable);

    @Query("{ 'status' : ?0, 'issuerSN' : { $in: ?1 } }")
    Page<CertificateRequest> findRequestsByStatusAndIssuerSerialNumbers(CertificateRequestStatus status, List<String> issuerSerialNumbers , Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match: { 'status': ?0 } }",
            "{ $count: 'count' }"
    })
    Long countByStatus(CertificateRequestStatus status);

    @Aggregation(pipeline = {
            "{ $match: { 'subjectUsername': ?0 } }",
            "{ $count: 'count' }"
    })
    Long countByUsername(String username);

    @Aggregation(pipeline = {
            "{ $match: { 'status': ?0, 'subjectUsername': ?1 } }",
            "{ $count: 'count' }"
    })
    Long countByStatusAndUsername(CertificateRequestStatus status, String username);
}
