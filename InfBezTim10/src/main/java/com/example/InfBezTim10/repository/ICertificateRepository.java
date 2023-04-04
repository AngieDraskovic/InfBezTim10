package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ICertificateRepository extends MongoRepository<Certificate, String> {

    @Query("{ 'SerialNumber' : ?0 }")
    Certificate findBySerialNumber(String SerialNumber);
}
