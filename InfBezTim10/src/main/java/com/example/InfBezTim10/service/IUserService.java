package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.User;

public interface IUserService extends IJPAService<User> {
    User findByEmail(String email);
}
