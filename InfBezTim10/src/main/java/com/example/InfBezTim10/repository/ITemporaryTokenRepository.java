package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.auth.TemporaryToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ITemporaryTokenRepository extends MongoRepository<TemporaryToken, String> {
    @Query("{'email': ?0 }")

    TemporaryToken findByEmail(String email);
}
