package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.Authority;
import com.example.InfBezTim10.model.AuthorityEnum;

public interface IAuthorityService  extends IJPAService<Authority> {

    Authority getAuthority(AuthorityEnum authorityEnum);
}
