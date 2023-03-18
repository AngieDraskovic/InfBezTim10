package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.Authority;
import com.example.InfBezTim10.model.AuthorityEnum;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.IAuthorityRepository;
import com.example.InfBezTim10.repository.IUserRepository;
import com.example.InfBezTim10.service.IAuthorityService;
import com.example.InfBezTim10.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthorityService extends JPAService<Authority> implements IAuthorityService {


    private IAuthorityRepository authorityRepository;
    @Autowired
    public AuthorityService(IAuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Override
    public Authority getAuthority(AuthorityEnum authorityEnum) {
        return switch (authorityEnum) {
            case ADMIN -> authorityRepository.findByAuthorityName("ROLE_ADMIN");
            case USER -> authorityRepository.findByAuthorityName("ROLE_USER");
        };
    }

    @Override
    protected JpaRepository<Authority, Long> getEntityRepository() {
        return authorityRepository;
    }
}
