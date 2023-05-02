package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.user.User;

public interface ITwillioService {

    void sendConfirmNumberSMS(User user, String activationId);
    void sendResetPasswordSMS(User user, String code);
}
