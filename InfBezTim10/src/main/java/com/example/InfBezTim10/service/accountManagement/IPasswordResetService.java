package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.dto.user.RenewPasswordDTO;
import com.example.InfBezTim10.dto.user.ResetPasswordDTO;
import com.example.InfBezTim10.exception.user.PasswordDoNotMatchException;
import com.example.InfBezTim10.model.user.PasswordReset;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.base.IJPAService;

import java.io.IOException;


public interface IPasswordResetService extends IJPAService<PasswordReset> {
    void deleteIfAlreadyExists(User user);

    void sendEmail(String userEmail, String confirmationMethod) throws IOException;

    void resetPassword(String email, ResetPasswordDTO resetPasswordDTO) throws PasswordDoNotMatchException;

    PasswordReset findByCode(String code);

    void renewPassword(String email, RenewPasswordDTO renewPasswordDTO) throws PasswordDoNotMatchException;
}
