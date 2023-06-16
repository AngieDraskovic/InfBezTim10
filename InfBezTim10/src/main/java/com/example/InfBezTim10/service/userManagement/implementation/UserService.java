package com.example.InfBezTim10.service.userManagement.implementation;

import com.example.InfBezTim10.exception.auth.PasswordExpiredException;
import com.example.InfBezTim10.exception.auth.UserNotVerifiedException;
import com.example.InfBezTim10.exception.user.UserNotFoundException;
import com.example.InfBezTim10.model.user.AccountStatus;
import com.example.InfBezTim10.model.user.Authority;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class UserService extends MongoService<User> implements IUserService, UserDetailsService {

    private final IUserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User with email {} not found.", email);
                    return new UserNotFoundException("User with email " + email + " not found.");
                });
    }


    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User findByOauthId(String oauthId) {
        return userRepository.findByOauthId(oauthId).orElseThrow(() -> new UserNotFoundException("User with oauth id " + oauthId + " not found."));
    }

    public void isUserVerified(String email) {
        User user = findByEmail(email);
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            logger.warn("User account is not verified for email: {}", email);
            throw new UserNotVerifiedException("User account is not verified.");
        }
    }

    @Override
    public boolean isPasswordExpired(String email) {
        User user = findByEmail(email);
        if (user.getLastPasswordResetDate() == null) {
            return true;
        }

        LocalDateTime lastResetDate = user.getLastPasswordResetDate();
        LocalDateTime expirationDate = lastResetDate.plusDays(30);
        //LocalDateTime expirationDate = lastResetDate.plusMinutes(2);    // TODO: ova vrijednost samo za provjere
        return expirationDate.isBefore(LocalDateTime.now());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Arrays.asList(user.getAuthority()));
    }


    @Override
    protected MongoRepository<User, String> getEntityRepository() {
        return this.userRepository;
    }
}
