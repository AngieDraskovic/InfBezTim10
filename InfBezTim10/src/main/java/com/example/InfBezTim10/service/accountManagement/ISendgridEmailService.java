package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.user.User;

import java.io.IOException;

public interface ISendgridEmailService {

    void sendConfirmEmailMessage(User toUser, String code) throws IOException;
    void sendNewPasswordMail(User toUser, String code) throws IOException;
    void sendTwoFactorAuthCodeMail(User user, String code) throws IOException;
}
