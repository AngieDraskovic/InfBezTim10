package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.dto.ResetPasswordDTO;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.exception.PasswordDoNotMatchException;
import com.example.InfBezTim10.model.PasswordReset;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;
import com.example.InfBezTim10.repository.IPasswordResetRepository;
import com.example.InfBezTim10.repository.IUserActivationRepository;
import com.example.InfBezTim10.service.IPasswordResetService;
import com.example.InfBezTim10.service.IUserActivationService;
import com.example.InfBezTim10.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService extends MongoService<PasswordReset> implements IPasswordResetService {

    private final IPasswordResetRepository passwordResetRepository;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetService(IPasswordResetRepository passwordResetRepository, IUserService userService, PasswordEncoder passwordEncoder) {
        this.passwordResetRepository = passwordResetRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    protected MongoRepository<PasswordReset, String> getEntityRepository() {
        return this.passwordResetRepository;
    }


    @Override
    public void deleteIfAlreadyExists(User user) {
        if (passwordResetRepository.existsByUser(user)) {
            passwordResetRepository.deleteByUser(user);
        }
    }

    @Override
    public PasswordReset save(PasswordReset reset) {
        return passwordResetRepository.save(reset);
    }

    @Override
    public void resetPassword(User user, ResetPasswordDTO resetPasswordDTO) throws PasswordDoNotMatchException {
        PasswordReset passwordReset = passwordResetRepository.findByCode(resetPasswordDTO.getCode());
        if (passwordReset == null) {
            throw new NotFoundException("Incorrect code! ");
        }
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getNewPasswordConfirm())) {
            throw new PasswordDoNotMatchException("Passwords do not match!  ");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userService.save(user);
        passwordResetRepository.delete(passwordReset);

    }

}
