package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.User;

import java.io.IOException;

public interface ISendgridEmailService {

    void sendConfirmEmailMessage(User toUser, String code) throws IOException;
}
