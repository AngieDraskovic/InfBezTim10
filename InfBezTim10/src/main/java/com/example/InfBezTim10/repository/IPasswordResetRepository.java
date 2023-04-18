package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.PasswordReset;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IPasswordResetRepository  extends MongoRepository<PasswordReset, String> {
    boolean existsByUser(User user);

    void deleteByUser(User user);

    @Query("{ 'code' : ?0 }")
    PasswordReset findByCode(String code);
}
