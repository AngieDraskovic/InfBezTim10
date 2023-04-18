package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.CertificateRequest;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IUserActivationRepository extends MongoRepository<UserActivation, String> {

    boolean existsByUser(User user);

    void deleteByUser(User user);

    @Query("{ 'activationId' : ?0 }")
    UserActivation findByActivationId(String activationId);

}
