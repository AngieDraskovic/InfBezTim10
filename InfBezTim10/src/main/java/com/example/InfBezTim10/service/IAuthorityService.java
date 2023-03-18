package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.Authority;
import com.example.InfBezTim10.model.AuthorityEnum;
import com.example.InfBezTim10.model.User;


// napravim enum
public interface IAuthorityService  extends IJPAService<Authority> {

    public Authority getAuthority(AuthorityEnum authorityEnum);
}
