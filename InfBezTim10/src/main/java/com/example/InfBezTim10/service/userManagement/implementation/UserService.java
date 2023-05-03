package com.example.InfBezTim10.service.userManagement.implementation;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.exception.user.UserNotFoundException;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserService extends MongoService<User> implements IUserService, UserDetailsService {

    private final IUserRepository userRepository;


    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found."));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = findByEmail(username);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Arrays.asList(user.getAuthority()));
    }

    @Override
    protected MongoRepository<User, String> getEntityRepository() {
        return this.userRepository;
    }


}
