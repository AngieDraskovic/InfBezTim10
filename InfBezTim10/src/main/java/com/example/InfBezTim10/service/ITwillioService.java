package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.User;

public interface ITwillioService {

    void sendConfirmNumberSMS(User user, String activationId);
    void sendResetPasswordSMS(User user, String code);
}
