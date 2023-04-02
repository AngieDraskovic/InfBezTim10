package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}
