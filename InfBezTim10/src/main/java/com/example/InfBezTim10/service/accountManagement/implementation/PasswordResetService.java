package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.dto.user.RenewPasswordDTO;
import com.example.InfBezTim10.dto.user.ResetPasswordDTO;
import com.example.InfBezTim10.exception.user.IncorrectCodeException;
import com.example.InfBezTim10.exception.user.PasswordDoNotMatchException;
import com.example.InfBezTim10.exception.user.PasswordResetNotFoundException;
import com.example.InfBezTim10.exception.user.PreviousPasswordException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class PasswordResetService extends MongoService<PasswordReset> implements IPasswordResetService {

    private final IPasswordResetRepository passwordResetRepository;
    private final IUserService userService;
    private final ISendgridEmailService sendgridEmailService;
    private final ITwillioService twillioService;
    private final PasswordEncoder passwordEncoder;
    private final Random rand = new Random();
    private static final Integer N = 4;


    @Autowired
    public PasswordResetService(IPasswordResetRepository passwordResetRepository, IUserService userService, ISendgridEmailService sendgridEmailService, ITwillioService twillioService) {
        this.passwordResetRepository = passwordResetRepository;
        this.userService = userService;
        this.sendgridEmailService = sendgridEmailService;
        this.twillioService = twillioService;
        this.passwordEncoder = new BCryptPasswordEncoder();
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
        reset.setCode(String.valueOf(rand.nextInt(Integer.MAX_VALUE)));
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
        PasswordReset passwordReset = findByCode(resetPasswordDTO.getCode());
        if(!passwordReset.getUser().equals(user)){
            throw new IncorrectCodeException("Incorrect code!  ");
        }
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getNewPasswordConfirm())) {
            throw new PasswordDoNotMatchException("Passwords do not match!  ");
        }

        List<String> previousNPasswords = rotation(user);
        String newPassword = resetPasswordDTO.getNewPassword();
        if (previousNPasswords.stream().anyMatch(oldPassword -> passwordEncoder.matches(newPassword, oldPassword))) {
            throw new PreviousPasswordException("You cannot use one of your previous passwords!");
        }

        user.setPreviousPasswords(rotation(user));
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setLastPasswordResetDate(LocalDateTime.now());
        userService.save(user);
        passwordResetRepository.delete(passwordReset);
    }

    @Override
    public PasswordReset findByCode(String code) {
        return passwordResetRepository.findByCode(code)
                .orElseThrow(() -> new PasswordResetNotFoundException("Password reset for code " + code + " does not exist!"));
    }

    @Override
    public void renewPassword(String email, RenewPasswordDTO passwordDTO) throws PasswordDoNotMatchException {
        User user = userService.findByEmail(email);
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getNewPasswordConfirm())) {
            throw new PasswordDoNotMatchException("Passwords do not match!");
        }
        List<String> previousNPasswords = rotation(user);
        String newPassword = passwordDTO.getNewPassword();
        if (previousNPasswords.stream().anyMatch(oldPassword -> passwordEncoder.matches(newPassword, oldPassword))) {
            throw new PreviousPasswordException("You cannot use one of your previous passwords!");
        }
        user.setPreviousPasswords(previousNPasswords);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordResetDate(LocalDateTime.now());
        userService.save(user);
    }


    public List<String> rotation(User user){
        if (user.getPreviousPasswords() == null) {
            user.setPreviousPasswords(new ArrayList<>());
        }
        List<String> previousNPasswords = new ArrayList<>(user.getPreviousPasswords());
        previousNPasswords.add(user.getPassword());
        if (previousNPasswords.size() > N) {
            previousNPasswords.remove(0);
        }
        return previousNPasswords;
    }


    @Override
    protected MongoRepository<PasswordReset, String> getEntityRepository() {
        return this.passwordResetRepository;
    }
}
