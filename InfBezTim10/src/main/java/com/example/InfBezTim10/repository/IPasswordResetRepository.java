package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.auth.PasswordReset;
import com.example.InfBezTim10.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IPasswordResetRepository  extends MongoRepository<PasswordReset, String> {
    Optional<PasswordReset> findByCode(String code);
    boolean existsByUser(User user);
    void deleteByUser(User user);
}
