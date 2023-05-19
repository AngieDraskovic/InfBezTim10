package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.certificate.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ICertificateRepository extends MongoRepository<Certificate, String> {
    @Query("{'serialNumber': ?0}")
    Optional<Certificate> findBySerialNumber(String SN);

    @Query("{'issuer': ?0}")
    List<Certificate> findSignedBy(String issuerSN);

    @Query("{'userEmail': ?0}")
    Page<Certificate> findByUserEmail(String userEmail, Pageable pageable);

    @Query("{'userEmail': ?0}")
    List<Certificate> findByUserEmail(String userEmail);

    @Query("{ 'status' : 'VALID', 'validFrom' : { $lte : ?0 }, 'validTo' : { $lte : ?0 } }")
    List<Certificate> findExpired(Date currentDate);

    @Aggregation(pipeline = {
            "{ $match: { 'status': ?0 } }",
            "{ $count: 'count' }"
    })
    Long countByStatus(CertificateStatus status);
}
