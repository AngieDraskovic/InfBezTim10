package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.model.user.AuthorityEnum;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.user.UserData;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.accountManagement.IOAuthService;
import com.example.InfBezTim10.service.userManagement.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class OAuthService implements IOAuthService {
    private final IUserRepository userRepository;
    private final IAuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OAuthService(IUserRepository userRepository, IAuthorityService authorityService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorityService = authorityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User processOAuthUser(String oauthId, UserData userData) {
        Optional<User> optionalUser = userRepository.findByOauthIdOrEmail(oauthId, userData.getEmail());
        if (optionalUser.isEmpty()) {
            return createOAuthUser(oauthId, userData);
        }

        User user = optionalUser.get();
        if (user.getOauthId() == null) {
            user.setOauthId(oauthId);
            userRepository.save(user);
        }

        return user;
    }

    private User createOAuthUser(String oauthId, UserData userData) {
        User user = User.createOAuthUser(
                userData.getEmail(),
                passwordEncoder.encode(generateRandomPassword()),
                userData.getName(),
                userData.getSurname(),
                oauthId
        );
        user.setAuthority(authorityService.getAuthority(AuthorityEnum.USER));
        return userRepository.save(user);
    }

    private String generateRandomPassword() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(30);
        for (int i = 0; i < 30; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
