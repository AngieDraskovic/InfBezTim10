package com.example.InfBezTim10.service.userManagement;

import com.example.InfBezTim10.exception.user.UserNotFoundException;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.base.IJPAService;

public interface IUserService extends IJPAService<User> {
    User findByEmail(String email);

    User findByOauthId(String oauthId) throws UserNotFoundException;

    boolean emailExists(String email);

    void isUserVerified(String email);

    void checkPasswordExpiration(String email);
}
