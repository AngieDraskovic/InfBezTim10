package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.certificate.Certificate;
import com.example.InfBezTim10.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ICertificateRepository extends MongoRepository<Certificate, String> {
    @Query("{'SerialNumber': ?0}")
    Optional<Certificate> findBySerialNumber(String SN);

    @Query("{'issuerSN': ?0}")
    List<Certificate> findCertificatesSignedBy(String issuerSN);

    @Query("{ 'status' : 'pending', 'validFrom' : { $lte : ?0 }, 'validTo' : { $gt : ?0 } }")
    List<Certificate> findCertificatesToBeValidated(Date currentDate);
}
