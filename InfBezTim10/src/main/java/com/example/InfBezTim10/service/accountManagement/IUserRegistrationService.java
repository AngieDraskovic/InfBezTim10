package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.user.User;

import java.io.IOException;

public interface IUserRegistrationService {
    User registerUser(User user, String confirmationMethod) throws IOException;
}
