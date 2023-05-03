package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.user.UserActivation;
import com.example.InfBezTim10.service.base.IJPAService;

public interface IUserActivationService extends IJPAService<UserActivation> {
    UserActivation create(User user) throws NotFoundException;

    void deleteIfAlreadyExists(User user);

    void activate(String activationId) throws NotFoundException;

    UserActivation findByActivationId(String activationId);
}
