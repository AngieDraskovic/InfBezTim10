package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.auth.TwoFactorAuth;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.base.IJPAService;

public interface ITwoFactorAuthenticationService  extends IJPAService<TwoFactorAuth> {

    void deleteIfAlreadyExists(User user);
    void sendCode(String userEmail, String confirmationMethod);
    TwoFactorAuth findByCode(String code);
    void verifyCode(String userEmail,  String code);
}
