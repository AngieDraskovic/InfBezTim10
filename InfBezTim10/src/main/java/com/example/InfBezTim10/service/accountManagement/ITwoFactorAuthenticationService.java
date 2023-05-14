package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.user.PasswordReset;
import com.example.InfBezTim10.model.user.TwoFactorAuth;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.base.IJPAService;

import java.io.IOException;

public interface ITwoFactorAuthenticationService  extends IJPAService<TwoFactorAuth> {

    void deleteIfAlreadyExists(User user);
    void sendCode(String userEmail, String confirmationMethod) throws IOException;
    TwoFactorAuth findByCode(String code);
    void verifyCode(String userEmail,  String code);
}
