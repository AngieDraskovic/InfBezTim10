package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.dto.user.ResetPasswordDTO;
import com.example.InfBezTim10.exception.user.PasswordDoNotMatchException;
import com.example.InfBezTim10.model.user.PasswordReset;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.repository.IPasswordResetRepository;
import com.example.InfBezTim10.service.accountManagement.IPasswordResetService;
import com.example.InfBezTim10.service.accountManagement.ISendgridEmailService;
import com.example.InfBezTim10.service.accountManagement.ITwillioService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PasswordResetService extends MongoService<PasswordReset> implements IPasswordResetService {

    private final IPasswordResetRepository passwordResetRepository;
    private final IUserService userService;
    private final ISendgridEmailService sendgridEmailService;
    private final ITwillioService twillioService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetService(IPasswordResetRepository passwordResetRepository, IUserService userService, ISendgridEmailService sendgridEmailService, ITwillioService twillioService, PasswordEncoder passwordEncoder) {
        this.passwordResetRepository = passwordResetRepository;
        this.userService = userService;
        this.sendgridEmailService = sendgridEmailService;
        this.twillioService = twillioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void deleteIfAlreadyExists(User user) {
        if (passwordResetRepository.existsByUser(user)) {
            passwordResetRepository.deleteByUser(user);
        }
    }

    @Override
    public void sendEmail(String userEmail, String confirmationMethod) throws IOException {
        User user = userService.findByEmail(userEmail);
        deleteIfAlreadyExists(user);

        PasswordReset reset = new PasswordReset();
        reset.setUser(user);
        reset.setCreationDate(LocalDateTime.now());
        save(reset);

        if (confirmationMethod.equalsIgnoreCase("email")) {
            sendgridEmailService.sendNewPasswordMail(user, reset.getCode());
        } else if (confirmationMethod.equalsIgnoreCase("sms")) {
            twillioService.sendResetPasswordSMS(user, reset.getCode());
        }
    }

    @Override
    public void resetPassword(String userEmail, ResetPasswordDTO resetPasswordDTO) throws PasswordDoNotMatchException {
        User user = userService.findByEmail(userEmail);
        PasswordReset passwordReset = findById(resetPasswordDTO.getCode());

        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getNewPasswordConfirm())) {
            throw new PasswordDoNotMatchException("Passwords do not match!  ");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userService.save(user);
        passwordResetRepository.delete(passwordReset);
    }

    @Override
    protected MongoRepository<PasswordReset, String> getEntityRepository() {
        return this.passwordResetRepository;
    }
}
