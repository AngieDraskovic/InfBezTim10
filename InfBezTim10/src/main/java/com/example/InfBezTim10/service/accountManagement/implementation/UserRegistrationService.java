package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.exception.auth.EmailAlreadyExistsException;
import com.example.InfBezTim10.model.user.AuthorityEnum;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.auth.UserActivation;
import com.example.InfBezTim10.service.accountManagement.ISendgridEmailService;
import com.example.InfBezTim10.service.accountManagement.ITwillioService;
import com.example.InfBezTim10.service.accountManagement.IUserActivationService;
import com.example.InfBezTim10.service.accountManagement.IUserRegistrationService;
import com.example.InfBezTim10.service.userManagement.IAuthorityService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UserRegistrationService implements IUserRegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final IUserService userService;
    private final IUserActivationService userActivationService;
    private final IAuthorityService authorityService;
    private final ISendgridEmailService sendgridEmailService;
    private final ITwillioService twillioService;
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);

    @Autowired
    public UserRegistrationService(PasswordEncoder passwordEncoder, IUserService userService, IUserActivationService userActivationService, IAuthorityService authorityService, ISendgridEmailService sendgridEmailService, ITwillioService twillioService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.userActivationService = userActivationService;
        this.authorityService = authorityService;
        this.sendgridEmailService = sendgridEmailService;
        this.twillioService = twillioService;
    }

    @Override
    public User registerUser(User user, String confirmationMethod) throws IOException {
        if (userService.emailExists(user.getEmail())) {
            logger.error("Error occur during registration as user with given email already exists: {}", user.getEmail());
            throw new EmailAlreadyExistsException("Email already exists.");
        } else {
            logger.info("User registration started for email: {}", user.getEmail());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setLastPasswordResetDate(LocalDateTime.now());
            user.setAuthority(authorityService.getAuthority(AuthorityEnum.USER));
            user = userService.save(user);
            UserActivation activation = userActivationService.create(user);

            if (confirmationMethod.equalsIgnoreCase("email")){
                logger.info("Confirmation email sent to user: {}", user.getEmail());
                sendgridEmailService.sendConfirmEmailMessage(user, activation.getActivationId());
            } else if (confirmationMethod.equalsIgnoreCase("sms")) {
                logger.info("Confirmation SMS sent to user: {}", user.getEmail());
                twillioService.sendConfirmNumberSMS(user, activation.getActivationId());
            }

            return user;
        }
    }
}
