package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ICertificateRepository extends MongoRepository<Certificate, String> {

    Certificate findBySerialNumber(String SerialNumber);
}
