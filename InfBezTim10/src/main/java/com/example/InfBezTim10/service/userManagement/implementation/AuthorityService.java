package com.example.InfBezTim10.service.userManagement.implementation;

import com.example.InfBezTim10.model.user.Authority;
import com.example.InfBezTim10.model.user.AuthorityEnum;
import com.example.InfBezTim10.repository.IAuthorityRepository;
import com.example.InfBezTim10.service.userManagement.IAuthorityService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorityService extends MongoService<Authority> implements IAuthorityService {

    private final IAuthorityRepository authorityRepository;

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
    protected MongoRepository<Authority, String> getEntityRepository() {
        return authorityRepository;
    }
}
