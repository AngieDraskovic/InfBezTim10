package com.example.InfBezTim10.service;

import com.example.InfBezTim10.dto.ResetPasswordDTO;
import com.example.InfBezTim10.exception.PasswordDoNotMatchException;
import com.example.InfBezTim10.model.PasswordReset;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;

public interface IPasswordResetService extends IJPAService<PasswordReset> {
    void deleteIfAlreadyExists(User user);
    void resetPassword(User user, ResetPasswordDTO resetPasswordDTO) throws PasswordDoNotMatchException;
}
