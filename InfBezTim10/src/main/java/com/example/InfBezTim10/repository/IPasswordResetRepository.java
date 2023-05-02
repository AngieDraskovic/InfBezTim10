package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.user.PasswordReset;
import com.example.InfBezTim10.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface IPasswordResetRepository  extends MongoRepository<PasswordReset, String> {
    boolean existsByUser(User user);

    void deleteByUser(User user);
}
