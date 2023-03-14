package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepositoryJPA extends JpaRepository<User, Integer> {
}
