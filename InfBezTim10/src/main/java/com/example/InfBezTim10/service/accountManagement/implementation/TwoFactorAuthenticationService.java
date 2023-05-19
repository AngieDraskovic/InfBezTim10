package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.exception.user.IncorrectCodeException;
import com.example.InfBezTim10.exception.user.TwoFactorCodeNotFoundException;
import com.example.InfBezTim10.model.user.TwoFactorAuth;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.repository.ITwoFactorAuthRepository;
import com.example.InfBezTim10.service.accountManagement.ISendgridEmailService;
import com.example.InfBezTim10.service.accountManagement.ITwillioService;
import com.example.InfBezTim10.service.accountManagement.ITwoFactorAuthenticationService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;


@Service
public class TwoFactorAuthenticationService extends MongoService<TwoFactorAuth> implements ITwoFactorAuthenticationService{

    private final IUserService userService;
    private final ISendgridEmailService sendgridEmailService;
    private final ITwillioService twillioService;
    private final Random rand = new Random();
    private final ITwoFactorAuthRepository twoFactorAuthRepository;

    public TwoFactorAuthenticationService(IUserService userService, ITwillioService twillioService,
                                          ISendgridEmailService sendgridEmailService, ITwoFactorAuthRepository twoFactorAuthRepository) {
        this.userService = userService;
        this.sendgridEmailService = sendgridEmailService;
        this.twillioService = twillioService;
        this.twoFactorAuthRepository = twoFactorAuthRepository;
    }

    @Override
    public void deleteIfAlreadyExists(User user) {
        if (twoFactorAuthRepository.existsByUser(user)) {
            twoFactorAuthRepository.deleteByUser(user);
        }
    }

    @Override
    public void sendCode(String userEmail, String confirmationMethod) throws IOException {
        User user = userService.findByEmail(userEmail);
        deleteIfAlreadyExists(user);

        TwoFactorAuth auth = new TwoFactorAuth();
        auth.setUser(user);
        auth.setCreationDate(LocalDateTime.now());
        auth.setCode(String.valueOf(rand.nextInt(Integer.MAX_VALUE)));
        save(auth);

        if (confirmationMethod.equalsIgnoreCase("email")) {
            sendgridEmailService.sendTwoFactorAuthCodeMail(user, auth.getCode());
        } else if (confirmationMethod.equalsIgnoreCase("sms")) {
            twillioService.sendTwoFactorAuthCodeSMS(user, auth.getCode());
        }
    }


    @Override
    public void verifyCode(String userEmail,  String code)
    {
        TwoFactorAuth auth = findByCode(code);
        if(!Objects.equals(auth.getUser().getEmail(), userEmail)){
            throw new IncorrectCodeException("Incorrect code!  ");
        }

    }

    @Override
    public TwoFactorAuth findByCode(String code) {
        return twoFactorAuthRepository.findByCode(code)
                .orElseThrow(() -> new TwoFactorCodeNotFoundException("Password reset for code " + code + " does not exist!"));
    }
    @Override
    protected MongoRepository<TwoFactorAuth, String> getEntityRepository() {
        return this.twoFactorAuthRepository;
    }
}
