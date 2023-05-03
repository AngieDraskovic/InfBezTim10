package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.user.UserActivation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface IUserActivationRepository extends MongoRepository<UserActivation, String> {
    Optional<UserActivation> findByActivationId(String activationId);
    boolean existsByUser(User user);
    void deleteByUser(User user);
}
