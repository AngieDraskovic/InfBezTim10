package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends MongoRepository<User, String> {
    @Query("{'email': ?0 }")
    Optional<User> findByEmail(String email);

    Optional<User> findByOauthId(String oauthId);

    @Query("{'$or':[ {'oauthId': ?0 }, {'email': ?1 } ]}")
    Optional<User> findByOauthIdOrEmail(String oauthId, String email);
}
