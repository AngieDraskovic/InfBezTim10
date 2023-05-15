package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.user.PasswordReset;
import com.example.InfBezTim10.model.user.TwoFactorAuth;
import com.example.InfBezTim10.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ITwoFactorAuthRepository extends MongoRepository<TwoFactorAuth, String> {

    Optional<TwoFactorAuth> findByCode(String code);
    boolean existsByUser(User user);
    void deleteByUser(User user);

}
