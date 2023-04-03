package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.exception.EmailAlreadyExistsException;
import com.example.InfBezTim10.model.AuthorityEnum;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.IAuthorityService;
import com.example.InfBezTim10.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserService extends JPAService<User> implements IUserService, UserDetailsService {

    private final IUserRepository userRepository;
    private final IAuthorityService authorityService;

    @Autowired
    public UserService(IUserRepository userRepository, IAuthorityService authorityService) {
        this.userRepository = userRepository;
        this.authorityService = authorityService;
    }

    @Override
    protected MongoRepository<User, String> getEntityRepository() {
        return this.userRepository;
    }

    @Override
    public User register(User user) {
        if (findByEmail(user.getEmail()) != null)
            throw new EmailAlreadyExistsException("Email is already taken");

        user.setAuthority(authorityService.getAuthority(AuthorityEnum.USER));
        return save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Arrays.asList(user.getAuthority()));
    }
}
