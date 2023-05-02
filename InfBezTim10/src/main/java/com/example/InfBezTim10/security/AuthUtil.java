package com.example.InfBezTim10.security;

import com.example.InfBezTim10.service.userManagement.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    private final IUserService userService;

    @Autowired
    public AuthUtil(IUserService userService) {
        this.userService = userService;
    }

    public boolean hasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(roleName));
    }
}

