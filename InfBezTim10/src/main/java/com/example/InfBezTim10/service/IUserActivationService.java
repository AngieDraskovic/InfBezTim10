package com.example.InfBezTim10.service;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;

public interface IUserActivationService extends IJPAService<UserActivation>{
    UserActivation create(User user) throws NotFoundException;

    void deleteIfAlreadyExists(User user);

    void activate(String activationId) throws NotFoundException;
}
