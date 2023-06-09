package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.user.Authority;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthorityRepository extends MongoRepository<Authority, String> {
    Authority findByAuthorityName(String authorityName);
}
